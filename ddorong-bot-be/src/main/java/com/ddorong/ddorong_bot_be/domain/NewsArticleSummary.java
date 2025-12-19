package com.ddorong.ddorong_bot_be.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "news_article_summary",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_article_language_summary", columnNames = {"article_id", "lang"})
        }
)
public class NewsArticleSummary {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;

    // NOTE: 너 엔티티는 컬럼명이 "lang" 이다. DB도 동일해야 함.
    @Column(name = "lang", nullable = false, length = 10)
    private String language;

    // NOTE: 너 엔티티는 컬럼명이 "summary" 이다. DB도 동일해야 함.
    @Column(name = "summary", nullable = false, columnDefinition = "text")
    private String summaryText;

    // DB에 provider NOT NULL 제약이 있으니 엔티티도 nullable=false로 맞추는 게 안전
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected NewsArticleSummary() {}

    // 기존 시그니처 유지 (호환성)
    public static NewsArticleSummary of(NewsArticle article, String language, String summaryText) {
        return of(article, language, summaryText, "LLM_TEAM", "UNKNOWN_MODEL", null);
    }

    // 신규 시그니처 (업서트에서 사용)
    public static NewsArticleSummary of(
            NewsArticle article,
            String language,
            String summaryText,
            String provider,
            String model,
            Integer latencyMs
    ) {
        NewsArticleSummary e = new NewsArticleSummary();
        e.article = article;
        e.language = language;
        e.summaryText = summaryText;
        e.provider = (provider == null || provider.isBlank()) ? "LLM_TEAM" : provider;
        e.model = (model == null || model.isBlank()) ? "UNKNOWN_MODEL" : model;
        e.createdAt = OffsetDateTime.now();
        return e;
    }

    public UUID getId() { return id; }
    public NewsArticle getArticle() { return article; }
    public String getLanguage() { return language; }
    public String getSummaryText() { return summaryText; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setSummaryText(String summaryText) { this.summaryText = summaryText; }

    public void setProvider(String provider) {
        this.provider = (provider == null || provider.isBlank()) ? "LLM_TEAM" : provider;
    }

    public void setModel(String model) {
        this.model = (model == null || model.isBlank()) ? "UNKNOWN_MODEL" : model;
    }
}
