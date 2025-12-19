package com.ddorong.ddorong_bot_be.domain.repo;

import com.ddorong.ddorong_bot_be.domain.NewsCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsCategoryRepository extends JpaRepository<NewsCategory, UUID> {
    Optional<NewsCategory> findByCode(String code);
}
