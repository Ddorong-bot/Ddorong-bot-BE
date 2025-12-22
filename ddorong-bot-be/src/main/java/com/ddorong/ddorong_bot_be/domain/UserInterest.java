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
@Table(name = "user_interest")
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    @Column(name = "is_include", nullable = false)
    private Boolean isInclude = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected UserInterest() {}

    // ========== Getter 메서드 ==========
    
    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Boolean getIsInclude() {
        return isInclude;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    // ========== Setter 메서드 ==========
    
    public void setUser(AppUser user) {
        this.user = user;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setIsInclude(Boolean isInclude) {
        this.isInclude = isInclude;
    }

    // ========== Helper 메서드 ==========
    
    public static UserInterest create(AppUser user, String type, String value, Boolean isInclude) {
        UserInterest interest = new UserInterest();
        interest.user = user;
        interest.type = type;
        interest.value = value;
        interest.isInclude = isInclude;
        interest.createdAt = OffsetDateTime.now();
        return interest;
    }
}