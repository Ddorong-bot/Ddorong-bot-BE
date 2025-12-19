package com.ddorong.ddorong_bot_be.admin;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "source_country_registry")
public class SourceCountryRegistryEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name="source_name", nullable=false, unique=true, length=200)
    private String sourceName;

    @Column(name="country_code", nullable=false, length=10)
    private String countryCode;

    public String getCountryCode() { return countryCode; }
}
