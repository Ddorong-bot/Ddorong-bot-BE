package com.ddorong.ddorong_bot_be.llmjob.dto;

import java.util.List;

public record LlmJobPullResponse(List<LlmJobItem> jobs) {}
