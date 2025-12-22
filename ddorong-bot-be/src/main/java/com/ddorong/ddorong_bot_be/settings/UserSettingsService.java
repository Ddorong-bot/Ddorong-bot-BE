package com.ddorong.ddorong_bot_be.settings;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.ddorong.ddorong_bot_be.domain.AppUser;
import com.ddorong.ddorong_bot_be.domain.UserChannel;
import com.ddorong.ddorong_bot_be.domain.UserDeliverySchedule;
import com.ddorong.ddorong_bot_be.domain.UserInterest;
import com.ddorong.ddorong_bot_be.domain.UserPreference;
import com.ddorong.ddorong_bot_be.domain.repo.AppUserRepository;
import com.ddorong.ddorong_bot_be.domain.repo.UserChannelRepository;
import com.ddorong.ddorong_bot_be.domain.repo.UserDeliveryScheduleRepository;
import com.ddorong.ddorong_bot_be.domain.repo.UserInterestRepository;
import com.ddorong.ddorong_bot_be.domain.repo.UserPreferenceRepository;
import com.ddorong.ddorong_bot_be.settings.dto.AddChannelRequest;
import com.ddorong.ddorong_bot_be.settings.dto.ChannelDto;
import com.ddorong.ddorong_bot_be.settings.dto.ChannelUpdate;
import com.ddorong.ddorong_bot_be.settings.dto.InterestDto;
import com.ddorong.ddorong_bot_be.settings.dto.PreferenceDto;
import com.ddorong.ddorong_bot_be.settings.dto.ScheduleDto;
import com.ddorong.ddorong_bot_be.settings.dto.TestMessageResponse;
import com.ddorong.ddorong_bot_be.settings.dto.UpdateChannelsRequest;
import com.ddorong.ddorong_bot_be.settings.dto.UpdateInterestsRequest;
import com.ddorong.ddorong_bot_be.settings.dto.UpdatePreferencesRequest;
import com.ddorong.ddorong_bot_be.settings.dto.UpdateScheduleRequest;
import com.ddorong.ddorong_bot_be.settings.dto.UserListResponse;
import com.ddorong.ddorong_bot_be.settings.dto.UserSettingsResponse;
import com.ddorong.ddorong_bot_be.settings.dto.UserSummary;

@Service
@Transactional(readOnly = true)
public class UserSettingsService {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsService.class);

    private final AppUserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final UserInterestRepository interestRepository;
    private final UserChannelRepository channelRepository;
    private final UserDeliveryScheduleRepository scheduleRepository;
    private final RestTemplate restTemplate;

    public UserSettingsService(
            AppUserRepository userRepository,
            UserPreferenceRepository preferenceRepository,
            UserInterestRepository interestRepository,
            UserChannelRepository channelRepository,
            UserDeliveryScheduleRepository scheduleRepository
    ) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.interestRepository = interestRepository;
        this.channelRepository = channelRepository;
        this.scheduleRepository = scheduleRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 모든 사용자 목록 조회
     */
    public UserListResponse getAllUsers() {
        List<UserSummary> users = userRepository.findAll().stream()
                .map(user -> new UserSummary(
                        user.getId(),
                        user.getCode(),
                        user.getDisplayName(),
                        user.getTimezone()
                ))
                .collect(Collectors.toList());

        return new UserListResponse(users);
    }

    /**
     * 특정 사용자의 전체 설정 조회
     */
    public UserSettingsResponse getUserSettings(String userCode) {
        AppUser user = userRepository.findByCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userCode));

        // 관심사 조회
        List<InterestDto> interests = interestRepository.findByUserId(user.getId()).stream()
                .map(i -> new InterestDto(
                        i.getId(),
                        i.getType(),
                        i.getValue(),
                        i.getIsInclude()
                ))
                .collect(Collectors.toList());

        // 채널 조회
        List<ChannelDto> channels = channelRepository.findAll().stream()
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(c -> new ChannelDto(
                        c.getId(),
                        c.getChannelType(),
                        c.getDestination(),
                        c.getIsEnabled()
                ))
                .collect(Collectors.toList());

        // 기본 설정 조회
        UserPreference pref = preferenceRepository.findByUserId(user.getId()).orElse(null);
        PreferenceDto preference = pref != null
                ? new PreferenceDto(pref.getLanguageTarget(), pref.getDigestSize())
                : new PreferenceDto("ko", 10);

        // 스케줄 조회
        ScheduleDto schedule = scheduleRepository.findByUserId(user.getId())
                .map(s -> new ScheduleDto(
                        s.getId(),
                        s.getCronExpr(),
                        ScheduleDto.cronToTime(s.getCronExpr()),
                        s.getNextRunAt(),
                        s.getIsEnabled()
                ))
                .orElse(null);

        return new UserSettingsResponse(
                user.getId(),
                user.getCode(),
                user.getDisplayName(),
                user.getTimezone(),
                preference,
                interests,
                channels,
                schedule
        );
    }

    /**
     * 스케줄 업데이트
     */
    @Transactional
    public ScheduleDto updateSchedule(String userCode, UpdateScheduleRequest request) {
        AppUser user = userRepository.findByCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userCode));

        UserDeliverySchedule schedule = scheduleRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserDeliverySchedule newSchedule = UserDeliverySchedule.create(user, "0 0 9 * * *");
                    return scheduleRepository.save(newSchedule);
                });

        // 발송 시간 변경
        if (request.deliveryTime() != null && !request.deliveryTime().isEmpty()) {
            String newCron = ScheduleDto.timeToCron(request.deliveryTime());
            schedule.setCronExpr(newCron);

            // 다음 실행 시간 재계산
            updateNextRunAt(schedule, user.getTimezone());
        }

        // 활성화 상태 변경
        if (request.isEnabled() != null) {
            schedule.setIsEnabled(request.isEnabled());
        }

        scheduleRepository.save(schedule);

        String newTime = ScheduleDto.cronToTime(schedule.getCronExpr());
        log.info("Schedule updated for user {}: time={}, enabled={}",
                userCode, newTime, schedule.getIsEnabled());

        return new ScheduleDto(
                schedule.getId(),
                schedule.getCronExpr(),
                newTime,
                schedule.getNextRunAt(),
                schedule.getIsEnabled()
        );
    }

    /**
     * 다음 실행 시간 계산
     */
    private void updateNextRunAt(UserDeliverySchedule schedule, String timezone) {
        try {
            CronExpression cron = CronExpression.parse(schedule.getCronExpr());
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone != null ? timezone : "Asia/Seoul"));
            ZonedDateTime next = cron.next(now);

            if (next != null) {
                schedule.setNextRunAt(next.toOffsetDateTime());
                log.debug("Next run calculated: {}", next);
            }
        } catch (Exception e) {
            log.error("Failed to calculate next run time: {}", e.getMessage());
        }
    }

    /**
     * 관심사 업데이트
     */
    @Transactional
    public void updateInterests(String userCode, UpdateInterestsRequest request) {
        AppUser user = userRepository.findByCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userCode));

        // 기존 관심사 모두 삭제
        interestRepository.deleteAll(interestRepository.findByUserId(user.getId()));

        // 새 관심사 추가
        for (InterestDto dto : request.interests()) {
            UserInterest interest = UserInterest.create(
                    user,
                    dto.type(),
                    dto.value(),
                    dto.isInclude() != null ? dto.isInclude() : true
            );
            interestRepository.save(interest);
        }
    }

    /**
     * 채널 업데이트
     */
    @Transactional
    public void updateChannels(String userCode, UpdateChannelsRequest request) {
        AppUser user = userRepository.findByCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userCode));

        for (ChannelUpdate dto : request.channels()) {
            channelRepository.findById(dto.id()).ifPresent(channel -> {
                if (dto.destination() != null) {
                    channel.setDestination(dto.destination());
                }
                if (dto.isEnabled() != null) {
                    channel.setIsEnabled(dto.isEnabled());
                }
                channelRepository.save(channel);
            });
        }
    }

    /**
     * 채널 추가
     */
    @Transactional
    public ChannelDto addChannel(String userCode, AddChannelRequest request) {
        AppUser user = userRepository.findByCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userCode));

        UserChannel newChannel = UserChannel.create(
                user,
                request.channelType(),
                request.destination()
        );

        UserChannel saved = channelRepository.save(newChannel);
        log.info("Added new channel: type={}, user={}", request.channelType(), userCode);

        return new ChannelDto(
                saved.getId(),
                saved.getChannelType(),
                saved.getDestination(),
                saved.getIsEnabled()
        );
    }

    /**
     * 채널 삭제
     */
    @Transactional
    public void deleteChannel(String userCode, UUID channelId) {
        AppUser user = userRepository.findByCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userCode));

        UserChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelId));

        if (!channel.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Channel does not belong to user");
        }

        channelRepository.delete(channel);
        log.info("Deleted channel: id={}, type={}, user={}", channelId, channel.getChannelType(), userCode);
    }

    /**
     * 채널 테스트 메시지 전송
     */
    public TestMessageResponse sendTestMessage(String userCode, UUID channelId) {
        AppUser user = userRepository.findByCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userCode));

        UserChannel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelId));

        if (!channel.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Channel does not belong to user");
        }

        if (!channel.getIsEnabled()) {
            return new TestMessageResponse(false, "채널이 비활성화 상태입니다.");
        }

        if (channel.getDestination() == null || channel.getDestination().isEmpty()) {
            return new TestMessageResponse(false, "채널 주소가 설정되지 않았습니다.");
        }

        String testMessage = String.format("🎉 뉴스봇 테스트 메시지입니다!\n\n👤 사용자: %s\n📢 채널: %s\n✅ 설정이 정상적으로 저장되었습니다.",
                user.getDisplayName(), channel.getChannelType());

        try {
            switch (channel.getChannelType()) {
                case "SLACK":
                    sendSlackMessage(channel.getDestination(), testMessage);
                    break;
                case "DISCORD":
                    sendDiscordMessage(channel.getDestination(), testMessage);
                    break;
                case "EMAIL":
                    return new TestMessageResponse(false, "이메일 전송은 아직 구현되지 않았습니다.");
                default:
                    return new TestMessageResponse(false, "지원하지 않는 채널 타입: " + channel.getChannelType());
            }

            log.info("Test message sent: channel={}, type={}, user={}", channelId, channel.getChannelType(), userCode);
            return new TestMessageResponse(true, "테스트 메시지가 전송되었습니다!");

        } catch (Exception e) {
            log.error("Failed to send test message: channel={}, error={}", channelId, e.getMessage(), e);
            return new TestMessageResponse(false, "메시지 전송 실패: " + e.getMessage());
        }
    }

    /**
     * Slack Webhook으로 메시지 전송
     */
    private void sendSlackMessage(String webhookUrl, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("text", message);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);

        log.info("Slack message sent to webhook");
    }

    /**
     * Discord Webhook으로 메시지 전송
     */
    private void sendDiscordMessage(String webhookUrl, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("content", message);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);

        log.info("Discord message sent to webhook");
    }

    /**
     * 기본 설정 업데이트
     */
    @Transactional
    public void updatePreferences(String userCode, UpdatePreferencesRequest request) {
        AppUser user = userRepository.findByCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userCode));

        UserPreference pref = preferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> UserPreference.create(user, "ko", 10));

        if (request.languageTarget() != null) {
            pref.setLanguageTarget(request.languageTarget());
        }
        if (request.digestSize() != null) {
            pref.setDigestSize(request.digestSize());
        }

        preferenceRepository.save(pref);
    }
}