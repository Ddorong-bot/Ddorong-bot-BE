package com.ddorong.ddorong_bot_be.llmjob;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ddorong.ddorong_bot_be.domain.LlmJob;
import com.ddorong.ddorong_bot_be.domain.NewsArticle;
import com.ddorong.ddorong_bot_be.domain.repo.LlmJobRepository;
import com.ddorong.ddorong_bot_be.domain.repo.NewsArticleLocalizedRepository;
import com.ddorong.ddorong_bot_be.domain.repo.NewsArticleRepository;
import com.ddorong.ddorong_bot_be.domain.repo.NewsArticleSummaryRepository;
import com.ddorong.ddorong_bot_be.llmjob.dto.LlmJobCompleteRequest;
import com.ddorong.ddorong_bot_be.llmjob.dto.LlmJobFailRequest;
import com.ddorong.ddorong_bot_be.llmjob.dto.LlmJobItem;
import com.ddorong.ddorong_bot_be.llmjob.dto.LlmJobPullResponse;

@Service
public class LlmJobService {

    private final LlmJobRepository llmJobRepository;
    private final NewsArticleRepository articleRepository;
    private final NewsArticleLocalizedRepository localizedRepository;
    private final NewsArticleSummaryRepository summaryRepository;

    public LlmJobService(
            LlmJobRepository llmJobRepository,
            NewsArticleRepository articleRepository,
            NewsArticleLocalizedRepository localizedRepository,
            NewsArticleSummaryRepository summaryRepository
    ) {
        this.llmJobRepository = llmJobRepository;
        this.articleRepository = articleRepository;
        this.localizedRepository = localizedRepository;
        this.summaryRepository = summaryRepository;
    }

    @Transactional
    public LlmJobPullResponse pull(int limit) {
        var jobs = llmJobRepository.lockPendingJobs(limit);

        // 잡은 즉시 상태 변경
        for (LlmJob job : jobs) {
            job.setStatus("IN_PROGRESS");
            job.setStartedAt(OffsetDateTime.now());
        }

        var items = jobs.stream().map(job -> {
            NewsArticle a = articleRepository.findById(job.getArticleId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid articleId: " + job.getArticleId()));

            return new LlmJobItem(
                    job.getId(),
                    a.getId(),
                    job.getTargetLanguage(),
                    a.getSource() != null ? a.getSource().getType() : null,
                    a.getSource() != null ? a.getSource().getName() : null,
                    a.getCategory() != null ? a.getCategory().getCode() : null,
                    a.getUrl(),
                    a.getTitle(),
                    a.getContent(),
                    a.getPublishedAt(),
                    a.getContentHash()
            );
        }).toList();

        return new LlmJobPullResponse(items);
    }

    @Transactional
    public void complete(UUID jobId, LlmJobCompleteRequest req) {
        var job = llmJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid jobId: " + jobId));

        if ("DONE".equals(job.getStatus())) {
            return;
        }

        NewsArticle article = articleRepository.findById(job.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid articleId: " + job.getArticleId()));

        String targetLang = job.getTargetLanguage();

        // === localized 저장 ===
        var loc = com.ddorong.ddorong_bot_be.domain.NewsArticleLocalized.of(
                article,
                targetLang,
                req.translatedTitle(),
                req.translatedContent(),
                "LLM_TEAM", // provider: 서버 고정값
                "UNKNOWN_MODEL" // model: 서버 고정값
        );
        localizedRepository.save(loc);

        // === summary 저장 ===
        var sum = com.ddorong.ddorong_bot_be.domain.NewsArticleSummary.of(
                article,
                targetLang,
                req.summary()
        );
        summaryRepository.save(sum);

        job.setStatus("DONE");
        job.setFinishedAt(OffsetDateTime.now());
        llmJobRepository.save(job);
    }

    @Transactional
    public void fail(UUID jobId, LlmJobFailRequest req) {
        var job = llmJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid jobId: " + jobId));

        if ("DONE".equals(job.getStatus())) {
            return;
        }

        job.setStatus(Boolean.TRUE.equals(req.isRetryable()) ? "FAILED_RETRYABLE" : "FAILED");
        job.setErrorCode(req.errorCode());
        job.setErrorMessage(req.errorMessage());
        job.setFinishedAt(OffsetDateTime.now());
        llmJobRepository.save(job);
    }
}
