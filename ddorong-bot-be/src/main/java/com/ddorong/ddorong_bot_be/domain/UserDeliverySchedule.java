package com.ddorong.ddorong_bot_be.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_delivery_schedule")
public class UserDeliverySchedule {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "cron_expr", nullable = false, length = 100)
    private String cronExpr;

    @Column(name = "next_run_at")
    private OffsetDateTime nextRunAt;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    protected UserDeliverySchedule() {}

    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getCronExpr() { return cronExpr; }
    public OffsetDateTime getNextRunAt() { return nextRunAt; }
    public Boolean getIsEnabled() { return isEnabled; }

    public void setNextRunAt(OffsetDateTime nextRunAt) {
        this.nextRunAt = nextRunAt;
    }
}
