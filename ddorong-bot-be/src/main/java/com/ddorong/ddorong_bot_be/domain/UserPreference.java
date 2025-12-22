package com.ddorong.ddorong_bot_be.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_preference")
public class UserPreference {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "language_target", nullable = false, length = 10)
    private String languageTarget = "ko";

    @Column(name = "digest_size", nullable = false)
    private Integer digestSize = 10;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    protected UserPreference() {}

    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getLanguageTarget() { return languageTarget; }
    public Integer getDigestSize() { return digestSize; }
}
