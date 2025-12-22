package com.ddorong.ddorong_bot_be.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 12시간마다 Python 뉴스 수집기/번역기 실행
 * - 자정(00:00): 수집 → 번역
 * - 정오(12:00): 수집 → 번역
 */
@Component
public class PythonCollectorScheduler {

    private static final Logger log = LoggerFactory.getLogger(PythonCollectorScheduler.class);

    @Value("${python.project.path:D:/AIFFEL/Ddorong_bot/python-collector}")
    private String pythonProjectPath;

    @Value("${python.executable:python}")
    private String pythonExecutable;

    // 쉼표로 구분된 수집기 스크립트들
    @Value("${python.collector.scripts:main.py}")
    private String collectorScriptsConfig;

    // 쉼표로 구분된 번역기 스크립트들
    @Value("${python.translator.scripts:translator.py}")
    private String translatorScriptsConfig;

    /**
     * 자정(00:00)에 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void runMidnightCollection() {
        log.info("========================================");
        log.info("=== [{}] MIDNIGHT collection START ===", LocalDateTime.now());
        log.info("========================================");
        runCollectionAndTranslation();
    }

    /**
     * 정오(12:00)에 실행
     */
    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
    public void runNoonCollection() {
        log.info("========================================");
        log.info("=== [{}] NOON collection START ===", LocalDateTime.now());
        log.info("========================================");
        runCollectionAndTranslation();
    }

    /**
     * 수집 + 번역 순차 실행
     */
    public void runCollectionAndTranslation() {
        try {
            List<String> collectorScripts = parseScripts(collectorScriptsConfig);
            List<String> translatorScripts = parseScripts(translatorScriptsConfig);

            int totalSteps = collectorScripts.size() + translatorScripts.size();
            int currentStep = 0;
            int successCount = 0;
            int failCount = 0;

            // Phase 1: 수집기 실행
            log.info(">>> Phase 1: Running {} collector(s)...", collectorScripts.size());
            for (String script : collectorScripts) {
                currentStep++;
                log.info("[{}/{}] Running collector: {}", currentStep, totalSteps, script);
                
                if (runPythonScript(script)) {
                    successCount++;
                } else {
                    failCount++;
                    log.warn("Collector failed: {}, continuing...", script);
                }
            }

            // Phase 2: 번역기 실행
            log.info(">>> Phase 2: Running {} translator(s)...", translatorScripts.size());
            for (String script : translatorScripts) {
                currentStep++;
                log.info("[{}/{}] Running translator: {}", currentStep, totalSteps, script);
                
                if (runPythonScript(script)) {
                    successCount++;
                } else {
                    failCount++;
                    log.warn("Translator failed: {}, continuing...", script);
                }
            }

            log.info("========================================");
            log.info("=== Collection COMPLETED: success={}, failed={} ===", successCount, failCount);
            log.info("========================================");

        } catch (Exception e) {
            log.error("Error during collection/translation: {}", e.getMessage(), e);
        }
    }

    /**
     * 쉼표로 구분된 스크립트 목록 파싱
     */
    private List<String> parseScripts(String config) {
        return Arrays.stream(config.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Python 스크립트 실행
     */
    private boolean runPythonScript(String scriptName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, scriptName);
            pb.directory(new File(pythonProjectPath));
            pb.redirectErrorStream(true);

            log.info("Executing: {} {} (in {})", pythonExecutable, scriptName, pythonProjectPath);
            long startTime = System.currentTimeMillis();

            Process process = pb.start();

            // 출력 읽기
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[{}] {}", scriptName, line);
                }
            }

            // 최대 30분 대기
            boolean finished = process.waitFor(30, TimeUnit.MINUTES);

            if (!finished) {
                log.error("Script timed out (30min): {}", scriptName);
                process.destroyForcibly();
                return false;
            }

            int exitCode = process.exitValue();
            long duration = (System.currentTimeMillis() - startTime) / 1000;

            if (exitCode == 0) {
                log.info("✓ {} completed ({}s)", scriptName, duration);
                return true;
            } else {
                log.error("✗ {} failed with exit code {} ({}s)", scriptName, exitCode, duration);
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to run {}: {}", scriptName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 수동 실행 - 전체
     */
    public void runManually() {
        log.info("=== Manual trigger at {} ===", LocalDateTime.now());
        runCollectionAndTranslation();
    }

    /**
     * 수동 실행 - 수집기만
     */
    public void runCollectorsOnly() {
        log.info("=== Manual COLLECTORS at {} ===", LocalDateTime.now());
        parseScripts(collectorScriptsConfig).forEach(this::runPythonScript);
    }

    /**
     * 수동 실행 - 번역기만
     */
    public void runTranslatorsOnly() {
        log.info("=== Manual TRANSLATORS at {} ===", LocalDateTime.now());
        parseScripts(translatorScriptsConfig).forEach(this::runPythonScript);
    }

    /**
     * 특정 스크립트 실행
     */
    public boolean runSpecificScript(String scriptName) {
        log.info("=== Running specific: {} ===", scriptName);
        return runPythonScript(scriptName);
    }
}
