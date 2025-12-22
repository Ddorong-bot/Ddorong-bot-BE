package com.ddorong.ddorong_bot_be.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_channel")
public class UserChannel {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "channel_type", nullable = false, length = 20)
    private String channelType; // EMAIL, SLACK, DISCORD

    @Column(nullable = false, length = 500)
    private String destination;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    protected UserChannel() {}

    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getChannelType() { return channelType; }
    public String getDestination() { return destination; }
    public Boolean getIsEnabled() { return isEnabled; }
}
