package com.ddorong.ddorong_bot_be.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "news_category")
public class NewsCategory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    protected NewsCategory() {}

    public UUID getId() { return id; }
    public String getCode() { return code; }
}
