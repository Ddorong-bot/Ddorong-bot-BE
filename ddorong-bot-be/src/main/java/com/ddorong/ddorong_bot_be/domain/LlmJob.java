package com.ddorong.ddorong_bot_be.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "llm_job")
public class LlmJob {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "article_id", columnDefinition = "uuid", nullable = false)
    private UUID articleId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "target_lang", length = 10)
    private String targetLanguage;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    protected LlmJob() {}

    public UUID getId() { return id; }
    public UUID getArticleId() { return articleId; }
    public String getStatus() { return status; }
    public String getTargetLanguage() { return targetLanguage; }

    public void setStatus(String status) { this.status = status; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public void setFinishedAt(OffsetDateTime finishedAt) { this.finishedAt = finishedAt; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
