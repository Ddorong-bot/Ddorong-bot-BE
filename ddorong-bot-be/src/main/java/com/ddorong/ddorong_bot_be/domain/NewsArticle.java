package com.ddorong.ddorong_bot_be.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "news_article")
public class NewsArticle {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private NewsSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private NewsCategory category;

    @Column(name = "external_id", length = 200)
    private String externalId;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(length = 200)
    private String author;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "fetched_at", nullable = false)
    private OffsetDateTime fetchedAt = OffsetDateTime.now();

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "content_hash", nullable = false, length = 64, columnDefinition = "char(64)")
    private String contentHash;

    @Column(nullable = false, length = 20)
    private String status = "RAW";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected NewsArticle() {}

    public static NewsArticle create(
            NewsSource source,
            NewsCategory category,
            String externalId,
            String url,
            String title,
            String content,
            String author,
            OffsetDateTime publishedAt,
            OffsetDateTime fetchedAt,
            String contentHash
    ) {
        var a = new NewsArticle();
        a.source = source;
        a.category = category;
        a.externalId = externalId;
        a.url = url;
        a.title = title;
        a.content = content;
        a.author = author;
        a.publishedAt = publishedAt;
        if (fetchedAt != null) a.fetchedAt = fetchedAt;
        a.contentHash = contentHash;
        return a;
    }

    public UUID getId() { return id; }
    public String getContentHash() { return contentHash; }

    public NewsSource getSource() {
        return this.source;
    }

    public NewsCategory getCategory() {
        return this.category;
    }

    public String getUrl() {
        return this.url;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public java.time.OffsetDateTime getPublishedAt() {
        return this.publishedAt;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
