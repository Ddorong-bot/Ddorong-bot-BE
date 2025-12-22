package com.ddorong.ddorong_bot_be.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_channel")
public class UserChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "channel_type", nullable = false, length = 20)
    private String channelType;

    @Column(name = "destination", length = 500)
    private String destination;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected UserChannel() {
    }

    // ========== Getter 메서드 ==========
    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getChannelType() {
        return channelType;
    }

    public String getDestination() {
        return destination;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ========== Setter 메서드 ==========
    public void setUser(AppUser user) {
        this.user = user;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public void setDestination(String destination) {
        this.destination = destination;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static UserChannel create(AppUser user, String channelType, String destination) {
        UserChannel channel = new UserChannel();
        channel.user = user;
        channel.channelType = channelType;
        channel.destination = destination;
        channel.isEnabled = true;
        channel.createdAt = OffsetDateTime.now();
        channel.updatedAt = OffsetDateTime.now();
        return channel;
    }
}
