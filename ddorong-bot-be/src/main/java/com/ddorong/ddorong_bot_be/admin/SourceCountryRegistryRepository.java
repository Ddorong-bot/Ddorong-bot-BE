package com.ddorong.ddorong_bot_be.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SourceCountryRegistryRepository extends JpaRepository<SourceCountryRegistryEntity, UUID> {

    @Query("select r.countryCode from SourceCountryRegistryEntity r where lower(r.sourceName) = lower(:name)")
    Optional<String> findCountryCodeByNameIgnoreCase(String name);
}
