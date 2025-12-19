package com.ddorong.ddorong_bot_be.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;
import java.util.List;

public record IngestionBulkRequest(
        Run run,
        @NotEmpty @Valid List<Article> articles
) {
    public record Run(
            @NotBlank String trigger,        // SCHEDULED | MANUAL
            OffsetDateTime fetchedAt,
            String batchKey
    ) {}

    public record Article(
            @NotBlank String categoryCode,
            @NotBlank String sourceName,
            @NotBlank String sourceType,      // RSS, API, SCRAPE
            String baseUrl,
            String externalId,
            @NotBlank String url,
            @NotBlank String title,
            @NotBlank String content,
            String author,
            OffsetDateTime publishedAt,
            @NotBlank @Pattern(regexp = "^[a-fA-F0-9]{64}$") String contentHash
    ) {}
}
