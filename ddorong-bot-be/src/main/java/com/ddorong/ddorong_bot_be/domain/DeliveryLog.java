package com.ddorong.ddorong_bot_be.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "delivery_log",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "article_id", "channel_type"})
    }
)
public class DeliveryLog {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_run_id", nullable = false)
    private DeliveryRun deliveryRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private NewsArticle article;

    @Column(name = "channel_type", nullable = false, length = 20)
    private String channelType;

    @Column(nullable = false, length = 500)
    private String destination;

    @Column(nullable = false, length = 30)
    private String status; // SENT, FAILED, SKIPPED_DUPLICATE, SKIPPED_NO_ENRICHED

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected DeliveryLog() {}

    public static DeliveryLog create(
            DeliveryRun deliveryRun,
            AppUser user,
            NewsArticle article,
            String channelType,
            String destination,
            String status
    ) {
        DeliveryLog log = new DeliveryLog();
        log.deliveryRun = deliveryRun;
        log.user = user;
        log.article = article;
        log.channelType = channelType;
        log.destination = destination;
        log.status = status;
        if ("SENT".equals(status)) {
            log.sentAt = OffsetDateTime.now();
        }
        return log;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
