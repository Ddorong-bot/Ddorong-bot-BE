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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_preference")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "language_target", nullable = false, length = 10)
    private String languageTarget = "ko";

    @Column(name = "digest_size", nullable = false)
    private Integer digestSize = 10;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected UserPreference() {}

    // ========== Getter 메서드 ==========
    
    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getLanguageTarget() {
        return languageTarget;
    }

    public Integer getDigestSize() {
        return digestSize;
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

    public void setLanguageTarget(String languageTarget) {
        this.languageTarget = languageTarget;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setDigestSize(Integer digestSize) {
        this.digestSize = digestSize;
        this.updatedAt = OffsetDateTime.now();
    }

    // ========== Helper 메서드 ==========
    
    public static UserPreference create(AppUser user, String languageTarget, Integer digestSize) {
        UserPreference pref = new UserPreference();
        pref.user = user;
        pref.languageTarget = languageTarget;
        pref.digestSize = digestSize;
        pref.createdAt = OffsetDateTime.now();
        pref.updatedAt = OffsetDateTime.now();
        return pref;
    }
}