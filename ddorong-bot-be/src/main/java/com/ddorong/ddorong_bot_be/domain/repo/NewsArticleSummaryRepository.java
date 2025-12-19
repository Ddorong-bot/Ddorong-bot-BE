package com.ddorong.ddorong_bot_be.domain.repo;

import com.ddorong.ddorong_bot_be.domain.NewsArticleSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsArticleSummaryRepository extends JpaRepository<NewsArticleSummary, UUID> {
    Optional<NewsArticleSummary> findByArticleIdAndLanguage(UUID articleId, String language);
}
