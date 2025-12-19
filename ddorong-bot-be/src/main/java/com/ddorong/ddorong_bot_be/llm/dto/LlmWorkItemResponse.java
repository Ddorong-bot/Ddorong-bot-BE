package com.ddorong.ddorong_bot_be.llm.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LlmWorkItemResponse(
        UUID articleId,
        String sourceType,
        String sourceName,
        String categoryCode,
        String url,
        String title,
        String content,
        OffsetDateTime publishedAt
) {}
