package com.ddorong.ddorong_bot_be.llm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ddorong.ddorong_bot_be.domain.NewsArticle;
import com.ddorong.ddorong_bot_be.domain.NewsArticleLocalized;
import com.ddorong.ddorong_bot_be.domain.NewsArticleSummary;
import com.ddorong.ddorong_bot_be.domain.repo.NewsArticleLocalizedRepository;
import com.ddorong.ddorong_bot_be.domain.repo.NewsArticleRepository;
import com.ddorong.ddorong_bot_be.domain.repo.NewsArticleSummaryRepository;
import com.ddorong.ddorong_bot_be.llm.dto.LlmPullResponse;
import com.ddorong.ddorong_bot_be.llm.dto.LlmResultUpsertRequest;
import com.ddorong.ddorong_bot_be.llm.dto.LlmResultUpsertResponse;
import com.ddorong.ddorong_bot_be.llm.dto.LlmWorkItemResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Service
public class LlmService {

    private final EntityManager em;
    private final NewsArticleRepository articleRepository;
    private final NewsArticleLocalizedRepository localizedRepository;
    private final NewsArticleSummaryRepository summaryRepository;

    public LlmService(
            EntityManager em,
            NewsArticleRepository articleRepository,
            NewsArticleLocalizedRepository localizedRepository,
            NewsArticleSummaryRepository summaryRepository
    ) {
        this.em = em;
        this.articleRepository = articleRepository;
        this.localizedRepository = localizedRepository;
        this.summaryRepository = summaryRepository;
    }

    @Transactional
    public LlmPullResponse pullWorkItems(String languageTarget, int limit) {

        String sql = """
                WITH picked AS (
                    SELECT a.id
                    FROM news_article a
                    WHERE a.status = 'RAW'
                      AND NOT EXISTS (
                          SELECT 1
                          FROM news_article_localized l
                          WHERE l.article_id = a.id
                            AND l.lang = :lang
                      )
                    ORDER BY a.published_at DESC NULLS LAST, a.created_at DESC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                )
                UPDATE news_article a
                SET status = 'LLM_IN_PROGRESS'
                FROM picked
                WHERE a.id = picked.id
                RETURNING a.id
                """;

        Query q = em.createNativeQuery(sql);
        q.setParameter("lang", languageTarget);
        q.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Object> rows = q.getResultList();

        List<UUID> ids = rows.stream().map(o -> (UUID) o).toList();

        if (ids.isEmpty()) {
            return new LlmPullResponse(languageTarget, limit, 0, List.of());
        }

        Map<UUID, NewsArticle> byId = articleRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(NewsArticle::getId, a -> a));

        List<LlmWorkItemResponse> items = new ArrayList<>();
        for (UUID id : ids) {
            NewsArticle a = byId.get(id);
            if (a == null) {
                continue;
            }

            String sourceType = (a.getSource() != null) ? a.getSource().getType() : null;
            String sourceName = (a.getSource() != null) ? a.getSource().getName() : null;
            String categoryCode = (a.getCategory() != null) ? a.getCategory().getCode() : null;

            items.add(new LlmWorkItemResponse(
                    a.getId(),
                    sourceType,
                    sourceName,
                    categoryCode,
                    a.getUrl(),
                    a.getTitle(),
                    a.getContent(),
                    a.getPublishedAt()
            ));
        }

        return new LlmPullResponse(languageTarget, limit, items.size(), items);
    }

    @Transactional
    public LlmPullResponse pullAllWorkItems(String languageTarget) {

        String sql = """
            WITH picked AS (
                SELECT a.id
                FROM news_article a
                WHERE a.status = 'RAW'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM news_article_localized l
                      WHERE l.article_id = a.id
                        AND l.lang = :lang
                  )
                ORDER BY a.published_at DESC NULLS LAST, a.created_at DESC
                FOR UPDATE SKIP LOCKED
            )
            UPDATE news_article a
            SET status = 'LLM_IN_PROGRESS'
            FROM picked
            WHERE a.id = picked.id
            RETURNING a.id
            """;

        Query q = em.createNativeQuery(sql);
        q.setParameter("lang", languageTarget);
        // LIMIT 없음

        @SuppressWarnings("unchecked")
        List<Object> rows = q.getResultList();

        List<UUID> ids = rows.stream().map(o -> (UUID) o).toList();

        if (ids.isEmpty()) {
            return new LlmPullResponse(languageTarget, -1, 0, List.of());
        }

        Map<UUID, NewsArticle> byId = articleRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(NewsArticle::getId, a -> a));

        List<LlmWorkItemResponse> items = new ArrayList<>();
        for (UUID id : ids) {
            NewsArticle a = byId.get(id);
            if (a == null) {
                continue;
            }

            String sourceType = (a.getSource() != null) ? a.getSource().getType() : null;
            String sourceName = (a.getSource() != null) ? a.getSource().getName() : null;
            String categoryCode = (a.getCategory() != null) ? a.getCategory().getCode() : null;

            items.add(new LlmWorkItemResponse(
                    a.getId(),
                    sourceType,
                    sourceName,
                    categoryCode,
                    a.getUrl(),
                    a.getTitle(),
                    a.getContent(),
                    a.getPublishedAt()
            ));
        }

        return new LlmPullResponse(languageTarget, -1, items.size(), items);
    }

    @Transactional
    public LlmResultUpsertResponse upsertResult(LlmResultUpsertRequest req) {
        UUID articleId = req.articleId();
        NewsArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + articleId));

        boolean localizedUpserted = upsertLocalized(
                article,
                req.languageTarget(),
                req.translatedTitle(),
                req.translatedContent(),
                "LLM_TEAM",
                req.modelName()
        );

        boolean summaryUpserted = false;
        if (req.summaryText() != null && !req.summaryText().isBlank()) {
            summaryUpserted = upsertSummary(
                    article,
                    req.languageTarget(),
                    req.summaryText(),
                    "LLM_TEAM", // provider 고정
                    req.modelName(), // modelName
                    req.latencyMs()
            );
        }

        if (localizedUpserted) {
            article.setStatus("LLM_DONE");
        }

        return new LlmResultUpsertResponse(articleId, req.languageTarget(), localizedUpserted, summaryUpserted);
    }

    private boolean upsertLocalized(NewsArticle article, String lang, String title, String content,
            String provider, String model) {

        Optional<NewsArticleLocalized> existing
                = localizedRepository.findByArticleIdAndLang(article.getId(), lang); // 네 repo 메서드명에 맞춰 수정

        if (existing.isPresent()) {
            NewsArticleLocalized e = existing.get();
            e.setTitleTranslated(title);
            e.setContentTranslated(content);
            e.setProvider(provider);
            e.setModel(model);
            return true;
        }

        NewsArticleLocalized created = NewsArticleLocalized.of(article, lang, title, content, provider, model);
        localizedRepository.save(created);
        return true;
    }

    private boolean upsertSummary(NewsArticle article, String lang, String summaryText,
            String provider, String model, Integer latencyMs) {

        Optional<NewsArticleSummary> existing
                = summaryRepository.findByArticleIdAndLanguage(article.getId(), lang);

        if (existing.isPresent()) {
            NewsArticleSummary e = existing.get();
            e.setSummaryText(summaryText);
            e.setProvider(provider);
            e.setModel(model);
            return true;
        }

        NewsArticleSummary created = NewsArticleSummary.of(article, lang, summaryText, provider, model, latencyMs);
        summaryRepository.save(created);
        return true;
    }

}
