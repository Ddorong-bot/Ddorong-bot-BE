package com.ddorong.ddorong_bot_be.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "news_source")
public class NewsSource {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(name = "base_url", columnDefinition = "text")
    private String baseUrl;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    public NewsSource() {}

    public UUID getId() { return id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }
}
