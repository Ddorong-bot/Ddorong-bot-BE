package com.ddorong.ddorong_bot_be.delivery.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 뉴스 아이템 DTO
 */
public class NewsItemDto {
    
    private UUID articleId;
    private String originalTitle;
    private String translatedTitle;
    private String summary;
    private String originalUrl;
    private String sourceName;
    private String categoryCode;
    private OffsetDateTime publishedAt;
    
    public NewsItemDto(
            UUID articleId,
            String originalTitle,
            String translatedTitle,
            String summary,
            String originalUrl,
            String sourceName,
            String categoryCode,
            OffsetDateTime publishedAt
    ) {
        this.articleId = articleId;
        this.originalTitle = originalTitle;
        this.translatedTitle = translatedTitle;
        this.summary = summary;
        this.originalUrl = originalUrl;
        this.sourceName = sourceName;
        this.categoryCode = categoryCode;
        this.publishedAt = publishedAt;
    }
    
    public UUID getArticleId() { return articleId; }
    public String getOriginalTitle() { return originalTitle; }
    public String getTranslatedTitle() { return translatedTitle; }
    public String getSummary() { return summary; }
    public String getOriginalUrl() { return originalUrl; }
    public String getSourceName() { return sourceName; }
    public String getCategoryCode() { return categoryCode; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
}
