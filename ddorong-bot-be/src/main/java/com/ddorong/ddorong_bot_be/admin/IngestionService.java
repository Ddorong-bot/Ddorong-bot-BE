package com.ddorong.ddorong_bot_be.admin;

import com.ddorong.ddorong_bot_be.admin.dto.IngestionBulkRequest;
import com.ddorong.ddorong_bot_be.admin.dto.IngestionBulkResponse;
import com.ddorong.ddorong_bot_be.domain.NewsArticle;
import com.ddorong.ddorong_bot_be.domain.NewsSource;
import com.ddorong.ddorong_bot_be.domain.repo.NewsArticleRepository;
import com.ddorong.ddorong_bot_be.domain.repo.NewsCategoryRepository;
import com.ddorong.ddorong_bot_be.domain.repo.NewsSourceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionService {

    private final NewsArticleRepository articleRepo;
    private final NewsSourceRepository sourceRepo;
    private final NewsCategoryRepository categoryRepo;
    private final SourceCountryRegistryRepository registryRepo; // country auto map

    public IngestionService(
            NewsArticleRepository articleRepo,
            NewsSourceRepository sourceRepo,
            NewsCategoryRepository categoryRepo,
            SourceCountryRegistryRepository registryRepo
    ) {
        this.articleRepo = articleRepo;
        this.sourceRepo = sourceRepo;
        this.categoryRepo = categoryRepo;
        this.registryRepo = registryRepo;
    }

    @Transactional
    public IngestionBulkResponse ingestBulk(IngestionBulkRequest req) {
        int received = req.articles().size();
        int inserted = 0;
        int duplicated = 0;

        var fetchedAt = (req.run() != null) ? req.run().fetchedAt() : null;

        for (var a : req.articles()) {
            // (1) category: code -> entity
            var category = categoryRepo.findByCode(a.categoryCode())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid categoryCode: " + a.categoryCode()));

            // (2) source: name -> find or create
            NewsSource source = sourceRepo.findByNameIgnoreCase(a.sourceName())
                    .orElseGet(() -> {
                        var s = new NewsSource();
                        s.setName(a.sourceName().trim());
                        s.setType(a.sourceType().trim());
                        s.setBaseUrl(a.baseUrl());

                        registryRepo.findCountryCodeByNameIgnoreCase(a.sourceName())
                                .ifPresent(s::setCountryCode);

                        return sourceRepo.save(s);
                    });

            // (3) dedupe by content_hash
            if (articleRepo.findByContentHash(a.contentHash()).isPresent()) {
                duplicated++;
                continue;
            }

            // (4) save article
            var entity = NewsArticle.create(
                    source,
                    category,
                    a.externalId(),
                    a.url(),
                    a.title(),
                    a.content(),
                    a.author(),
                    a.publishedAt(),
                    fetchedAt,
                    a.contentHash()
            );

            try {
                articleRepo.save(entity);
                inserted++;
            } catch (DataIntegrityViolationException e) {
                duplicated++;
            }
        }

        // ingestion_run 테이블도 있지만, DB 동결이라 이번 단계에서는 null 반환
        return new IngestionBulkResponse(null, received, inserted, duplicated);
    }
}
