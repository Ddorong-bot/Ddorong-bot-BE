package com.ddorong.ddorong_bot_be.delivery;

import com.ddorong.ddorong_bot_be.delivery.channel.ChannelSendException;
import com.ddorong.ddorong_bot_be.delivery.channel.ChannelSender;
import com.ddorong.ddorong_bot_be.delivery.dto.*;
import com.ddorong.ddorong_bot_be.domain.*;
import com.ddorong.ddorong_bot_be.domain.repo.*;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final EntityManager em;
    private final AppUserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final UserInterestRepository interestRepository;
    private final UserChannelRepository channelRepository;
    private final NewsArticleRepository articleRepository;
    private final NewsArticleLocalizedRepository localizedRepository;
    private final NewsArticleSummaryRepository summaryRepository;
    private final DeliveryRunRepository deliveryRunRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final Map<String, ChannelSender> channelSenders;

    public DeliveryService(
            EntityManager em,
            AppUserRepository userRepository,
            UserPreferenceRepository preferenceRepository,
            UserInterestRepository interestRepository,
            UserChannelRepository channelRepository,
            NewsArticleRepository articleRepository,
            NewsArticleLocalizedRepository localizedRepository,
            NewsArticleSummaryRepository summaryRepository,
            DeliveryRunRepository deliveryRunRepository,
            DeliveryLogRepository deliveryLogRepository,
            List<ChannelSender> senders
    ) {
        this.em = em;
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.interestRepository = interestRepository;
        this.channelRepository = channelRepository;
        this.articleRepository = articleRepository;
        this.localizedRepository = localizedRepository;
        this.summaryRepository = summaryRepository;
        this.deliveryRunRepository = deliveryRunRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        
        this.channelSenders = senders.stream()
                .collect(Collectors.toMap(ChannelSender::getChannelType, s -> s));
    }

    /**
     * 특정 사용자에게 즉시 발송 (관리자 테스트용)
     */
    @Transactional
    public DeliveryTriggerResponse deliverToUser(DeliveryTriggerRequest request) {
        AppUser user = userRepository.findByCode(request.userCode())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userCode()));

        OffsetDateTime now = OffsetDateTime.now();
        DeliveryRun run = DeliveryRun.create(user, now);
        deliveryRunRepository.save(run);

        // 사용자 설정 조회
        UserPreference pref = preferenceRepository.findByUserId(user.getId()).orElse(null);
        String targetLang = pref != null ? pref.getLanguageTarget() : "ko";
        int digestSize = pref != null ? pref.getDigestSize() : 10;

        // 관심사 기반 뉴스 필터링
        List<NewsArticle> articles = findMatchingArticles(user, targetLang, digestSize);
        
        // 채널별 발송
        List<UserChannel> channels = channelRepository.findByUserIdAndIsEnabledTrue(user.getId());
        
        // 특정 채널만 지정한 경우 필터링
        if (request.channelType() != null) {
            channels = channels.stream()
                    .filter(ch -> ch.getChannelType().equals(request.channelType()))
                    .toList();
        }

        int sent = 0;
        int failed = 0;

        for (UserChannel channel : channels) {
            try {
                if (Boolean.TRUE.equals(request.dryRun())) {
                    log.info("[DRY RUN] Would send to {} via {}", channel.getDestination(), channel.getChannelType());
                    sent++;
                } else {
                    sendToChannel(run, user, channel, articles, targetLang);
                    sent++;
                }
            } catch (Exception e) {
                log.error("Failed to send to channel {}: {}", channel.getChannelType(), e.getMessage());
                failed++;
            }
        }

        String status = (failed == 0) ? "SUCCESS" : (sent > 0) ? "PARTIAL" : "FAILED";
        run.setStatus(status);
        run.setFinishedAt(OffsetDateTime.now());

        return new DeliveryTriggerResponse(
                run.getId(),
                user.getCode(),
                articles.size(),
                sent,
                failed,
                status,
                request.dryRun()
        );
    }

    /**
     * 채널로 뉴스 발송
     */
    private void sendToChannel(
            DeliveryRun run,
            AppUser user,
            UserChannel channel,
            List<NewsArticle> articles,
            String targetLang
    ) throws ChannelSendException {
        
        String channelType = channel.getChannelType();
        
        // 이미 발송된 기사 제외
        List<UUID> sentArticleIds = deliveryLogRepository.findSentArticleIds(user.getId(), channelType);
        List<NewsArticle> unsent = articles.stream()
                .filter(a -> !sentArticleIds.contains(a.getId()))
                .toList();

        if (unsent.isEmpty()) {
            log.info("No new articles to send to user {} via {}", user.getCode(), channelType);
            return;
        }

        // NewsDigest 생성
        NewsDigest digest = buildDigest(user, unsent, targetLang);

        // 채널 발송
        ChannelSender sender = channelSenders.get(channelType);
        if (sender == null) {
            throw new ChannelSendException("No sender for channel type: " + channelType);
        }

        sender.send(channel.getDestination(), digest);

        // 발송 로그 기록
        for (NewsArticle article : unsent) {
            DeliveryLog log = DeliveryLog.create(
                    run, user, article, channelType, channel.getDestination(), "SENT"
            );
            deliveryLogRepository.save(log);
        }
    }

    /**
     * 사용자 관심사에 맞는 뉴스 찾기
     */
    private List<NewsArticle> findMatchingArticles(AppUser user, String targetLang, int limit) {
        List<UserInterest> interests = interestRepository.findByUserId(user.getId());
        
        if (interests.isEmpty()) {
            // 관심사 없으면 최신 뉴스 반환
            return articleRepository.findAll().stream()
                    .filter(a -> "LLM_DONE".equals(a.getStatus()))
                    .sorted((a, b) -> {
                        OffsetDateTime t1 = a.getPublishedAt() != null ? a.getPublishedAt() : OffsetDateTime.MIN;
                        OffsetDateTime t2 = b.getPublishedAt() != null ? b.getPublishedAt() : OffsetDateTime.MIN;
                        return t2.compareTo(t1);
                    })
                    .limit(limit)
                    .toList();
        }

        // 카테고리 관심사
        Set<String> categories = interests.stream()
                .filter(i -> "CATEGORY".equals(i.getType()) && i.getIsInclude())
                .map(UserInterest::getValue)
                .collect(Collectors.toSet());

        // 키워드 관심사
        Set<String> keywords = interests.stream()
                .filter(i -> "KEYWORD".equals(i.getType()) && i.getIsInclude())
                .map(i -> i.getValue().toLowerCase())
                .collect(Collectors.toSet());

        return articleRepository.findAll().stream()
                .filter(a -> "LLM_DONE".equals(a.getStatus()))
                .filter(a -> matchesInterest(a, categories, keywords))
                .sorted((a, b) -> {
                    OffsetDateTime t1 = a.getPublishedAt() != null ? a.getPublishedAt() : OffsetDateTime.MIN;
                    OffsetDateTime t2 = b.getPublishedAt() != null ? b.getPublishedAt() : OffsetDateTime.MIN;
                    return t2.compareTo(t1);
                })
                .limit(limit)
                .toList();
    }

    private boolean matchesInterest(NewsArticle article, Set<String> categories, Set<String> keywords) {
        // 카테고리 매칭
        if (!categories.isEmpty()) {
            String categoryCode = article.getCategory() != null ? article.getCategory().getCode() : null;
            if (categoryCode != null && categories.contains(categoryCode)) {
                return true;
            }
        }

        // 키워드 매칭 (제목 또는 본문에 포함)
        if (!keywords.isEmpty()) {
            String titleLower = article.getTitle().toLowerCase();
            String contentLower = article.getContent().toLowerCase();
            
            for (String keyword : keywords) {
                if (titleLower.contains(keyword) || contentLower.contains(keyword)) {
                    return true;
                }
            }
        }

        return categories.isEmpty() && keywords.isEmpty();
    }

    /**
     * NewsDigest 빌드
     */
    private NewsDigest buildDigest(AppUser user, List<NewsArticle> articles, String targetLang) {
        List<NewsItemDto> items = new ArrayList<>();

        for (NewsArticle article : articles) {
            String translatedTitle = article.getTitle();
            String summary = "";

            // 번역 제목 조회
            localizedRepository.findByArticleIdAndLang(article.getId(), targetLang)
                    .ifPresent(loc -> {
                        // translatedTitle을 업데이트하려면 별도 변수 필요
                    });

            // 요약 조회
            summaryRepository.findByArticleIdAndLanguage(article.getId(), targetLang)
                    .ifPresent(sum -> {
                        // summary 업데이트
                    });

            // 최종적으로 번역/요약을 반영한 DTO 생성
            NewsArticleLocalized localized = localizedRepository
                    .findByArticleIdAndLang(article.getId(), targetLang)
                    .orElse(null);
            
            NewsArticleSummary summaryEntity = summaryRepository
                    .findByArticleIdAndLanguage(article.getId(), targetLang)
                    .orElse(null);

            items.add(new NewsItemDto(
                    article.getId(),
                    article.getTitle(),
                    localized != null ? localized.getTitleTranslated() : article.getTitle(),
                    summaryEntity != null ? summaryEntity.getSummaryText() : article.getContent().substring(0, Math.min(200, article.getContent().length())),
                    article.getUrl(),
                    article.getSource() != null ? article.getSource().getName() : "Unknown",
                    article.getCategory() != null ? article.getCategory().getCode() : "unknown",
                    article.getPublishedAt()
            ));
        }

        String title = String.format("Daily Digest - %s (%d articles)", user.getDisplayName(), items.size());
        return new NewsDigest(title, user.getCode(), items);
    }
}
