package com.ddorong.ddorong_bot_be.llm.dto;

import java.util.List;

public record LlmPullResponse(
        String languageTarget,
        int requested,
        int pulled,
        List<LlmWorkItemResponse> items
) {}
