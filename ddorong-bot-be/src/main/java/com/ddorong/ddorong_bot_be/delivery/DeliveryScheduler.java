package com.ddorong.ddorong_bot_be.delivery;

import com.ddorong.ddorong_bot_be.delivery.dto.DeliveryTriggerRequest;
import com.ddorong.ddorong_bot_be.domain.UserDeliverySchedule;
import com.ddorong.ddorong_bot_be.domain.repo.UserDeliveryScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * 주기적으로 사용자 발송 스케줄을 체크하고 실행하는 스케줄러
 */
@Component
public class DeliveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeliveryScheduler.class);

    private final UserDeliveryScheduleRepository scheduleRepository;
    private final DeliveryService deliveryService;

    public DeliveryScheduler(
            UserDeliveryScheduleRepository scheduleRepository,
            DeliveryService deliveryService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.deliveryService = deliveryService;
    }

    /**
     * 매 1분마다 실행
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void checkAndDeliverScheduled() {
        log.debug("Checking delivery schedules...");

        OffsetDateTime now = OffsetDateTime.now();
        List<UserDeliverySchedule> dueSchedules = scheduleRepository.findDueSchedules(now);

        if (dueSchedules.isEmpty()) {
            log.debug("No schedules due at this time");
            return;
        }

        log.info("Found {} schedule(s) due for delivery", dueSchedules.size());

        for (UserDeliverySchedule schedule : dueSchedules) {
            try {
                String userCode = schedule.getUser().getCode();
                log.info("Executing scheduled delivery for user: {}", userCode);

                DeliveryTriggerRequest request = new DeliveryTriggerRequest(userCode, null, false);
                deliveryService.deliverToUser(request);

                // 다음 실행 시간 계산
                updateNextRunAt(schedule);
                scheduleRepository.save(schedule);

            } catch (Exception e) {
                log.error("Failed to execute scheduled delivery for schedule {}: {}",
                        schedule.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * cron 표현식을 기반으로 다음 실행 시간 계산
     */
    private void updateNextRunAt(UserDeliverySchedule schedule) {
        try {
            CronExpression cron = CronExpression.parse(schedule.getCronExpr());
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(schedule.getUser().getTimezone()));
            ZonedDateTime next = cron.next(now);
            
            if (next != null) {
                schedule.setNextRunAt(next.toOffsetDateTime());
                log.debug("Next run for user {} scheduled at {}", 
                        schedule.getUser().getCode(), next);
            } else {
                log.warn("Could not calculate next run time for schedule {}", schedule.getId());
            }
        } catch (Exception e) {
            log.error("Failed to parse cron expression '{}': {}", 
                    schedule.getCronExpr(), e.getMessage());
        }
    }
}
