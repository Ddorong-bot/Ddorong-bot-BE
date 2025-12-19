package com.ddorong.ddorong_bot_be.llmjob.dto;

public record LlmJobCompleteRequest(
        String detectedSourceLanguage,
        String translatedTitle,
        String translatedContent,
        String summary,
        String modelProvider,
        String modelName,
        String promptVersion,
        Usage usage
) {
    public record Usage(Integer inputTokens, Integer outputTokens) {}
}
