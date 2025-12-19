package com.ddorong.ddorong_bot_be.domain.repo;

import com.ddorong.ddorong_bot_be.domain.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsSourceRepository extends JpaRepository<NewsSource, UUID> {
    Optional<NewsSource> findByNameIgnoreCase(String name);
}
