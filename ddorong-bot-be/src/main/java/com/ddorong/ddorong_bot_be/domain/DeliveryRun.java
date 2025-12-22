package com.ddorong.ddorong_bot_be.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_run")
public class DeliveryRun {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt = OffsetDateTime.now();

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(nullable = false, length = 20)
    private String status; // SUCCESS, PARTIAL, FAILED

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected DeliveryRun() {}

    public static DeliveryRun create(AppUser user, OffsetDateTime scheduledAt) {
        DeliveryRun run = new DeliveryRun();
        run.user = user;
        run.scheduledAt = scheduledAt;
        run.status = "RUNNING";
        return run;
    }

    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
    public void setFinishedAt(OffsetDateTime finishedAt) { this.finishedAt = finishedAt; }
}
