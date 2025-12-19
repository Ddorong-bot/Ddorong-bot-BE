package com.ddorong.ddorong_bot_be.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "news_article_localized")
public class NewsArticleLocalized {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;

    @Column(name = "lang", nullable = false, length = 10)
    private String lang;

    @Column(name = "title_translated", nullable = false, columnDefinition = "text")
    private String titleTranslated;

    @Column(name = "content_translated", nullable = false, columnDefinition = "text")
    private String contentTranslated;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected NewsArticleLocalized() {
    }

    public void setTitleTranslated(String titleTranslated) {
        this.titleTranslated = titleTranslated;
    }

    public void setContentTranslated(String contentTranslated) {
        this.contentTranslated = contentTranslated;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public static NewsArticleLocalized of(
            NewsArticle article,
            String lang,
            String titleTranslated,
            String contentTranslated,
            String provider,
            String model
    ) {
        var e = new NewsArticleLocalized();
        e.article = article;
        e.lang = lang;
        e.titleTranslated = titleTranslated;
        e.contentTranslated = contentTranslated;
        e.provider = provider;
        e.model = model;
        return e;
    }

    public UUID getId() {
        return id;
    }

    public NewsArticle getArticle() {
        return article;
    }

    public String getLang() {
        return lang;
    }

    public String getTitleTranslated() {
        return titleTranslated;
    }

    public String getContentTranslated() {
        return contentTranslated;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
