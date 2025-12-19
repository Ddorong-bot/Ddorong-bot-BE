package com.ddorong.ddorong_bot_be.llm.dto;

import java.util.UUID;

public record LlmResultUpsertRequest(
  UUID articleId,
  String languageTarget,
  String translatedTitle,
  String translatedContent,
  String summaryText,
  String modelName,
  Integer latencyMs
) {}