package com.ddorong.ddorong_bot_be.scheduler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Python 수집기/번역기 수동 실행 API
 */
@RestController
@RequestMapping("/api/admin/collector")
public class CollectorController {

    private final PythonCollectorScheduler scheduler;

    public CollectorController(PythonCollectorScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 전체 실행 (수집 + 번역)
     * POST /api/admin/collector/run
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runAll() {
        new Thread(scheduler::runManually).start();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수집 + 번역 작업이 시작되었습니다. 로그를 확인하세요."
        ));
    }

    /**
     * 수집기만 실행
     * POST /api/admin/collector/run/collectors
     */
    @PostMapping("/run/collectors")
    public ResponseEntity<Map<String, Object>> runCollectors() {
        new Thread(scheduler::runCollectorsOnly).start();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "수집 작업이 시작되었습니다."
        ));
    }

    /**
     * 번역기만 실행
     * POST /api/admin/collector/run/translators
     */
    @PostMapping("/run/translators")
    public ResponseEntity<Map<String, Object>> runTranslators() {
        new Thread(scheduler::runTranslatorsOnly).start();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "번역 작업이 시작되었습니다."
        ));
    }

    /**
     * 특정 스크립트 실행
     * POST /api/admin/collector/run/script?name=collector.py
     */
    @PostMapping("/run/script")
    public ResponseEntity<Map<String, Object>> runScript(@RequestParam String name) {
        new Thread(() -> scheduler.runSpecificScript(name)).start();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "스크립트 '" + name + "'가 시작되었습니다."
        ));
    }

    /**
     * 스케줄 상태 확인
     * GET /api/admin/collector/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "scheduled", true,
                "times", "00:00, 12:00 (Asia/Seoul)",
                "description", "매일 자정과 정오에 뉴스 수집/번역 실행"
        ));
    }
}
