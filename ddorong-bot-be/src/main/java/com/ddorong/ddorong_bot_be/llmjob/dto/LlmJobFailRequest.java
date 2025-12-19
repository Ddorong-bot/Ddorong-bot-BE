package com.ddorong.ddorong_bot_be.llmjob.dto;

public record LlmJobFailRequest(
        String errorCode,
        String errorMessage,
        Boolean isRetryable
) {}
