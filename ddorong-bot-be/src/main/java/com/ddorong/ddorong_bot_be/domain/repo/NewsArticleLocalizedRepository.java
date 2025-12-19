package com.ddorong.ddorong_bot_be.domain.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ddorong.ddorong_bot_be.domain.NewsArticleLocalized;

public interface NewsArticleLocalizedRepository extends JpaRepository<NewsArticleLocalized, UUID> {
    Optional<NewsArticleLocalized> findByArticleIdAndLang(UUID articleId, String lang);
}
