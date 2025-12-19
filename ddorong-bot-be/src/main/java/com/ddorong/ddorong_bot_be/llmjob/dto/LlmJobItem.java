package com.ddorong.ddorong_bot_be.llmjob.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LlmJobItem(
        UUID jobId,
        UUID articleId,
        String targetLanguage,
        String sourceType,
        String sourceName,
        String categoryCode,
        String url,
        String title,
        String content,
        OffsetDateTime publishedAt,
        String contentHash
) {}
