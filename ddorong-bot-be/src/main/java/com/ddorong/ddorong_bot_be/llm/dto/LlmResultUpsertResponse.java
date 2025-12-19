package com.ddorong.ddorong_bot_be.llm.dto;

import java.util.UUID;

public record LlmResultUpsertResponse(
        UUID articleId,
        String languageTarget,
        boolean localizedUpserted,
        boolean summaryUpserted
) {}
