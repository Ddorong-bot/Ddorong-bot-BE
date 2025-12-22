package com.ddorong.ddorong_bot_be.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_interest")
public class UserInterest {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 20)
    private String type; // CATEGORY, KEYWORD, SOURCE

    @Column(nullable = false, length = 200)
    private String value;

    @Column(name = "is_include", nullable = false)
    private Boolean isInclude = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected UserInterest() {}

    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getType() { return type; }
    public String getValue() { return value; }
    public Boolean getIsInclude() { return isInclude; }
}
