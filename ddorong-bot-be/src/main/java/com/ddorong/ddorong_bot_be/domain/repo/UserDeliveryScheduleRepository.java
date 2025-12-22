package com.ddorong.ddorong_bot_be.domain.repo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ddorong.ddorong_bot_be.domain.UserDeliverySchedule;

public interface UserDeliveryScheduleRepository extends JpaRepository<UserDeliverySchedule, UUID> {
    
    /**
     * 발송 시간이 도래한 스케줄 조회
     */
    @Query("""
        SELECT s FROM UserDeliverySchedule s
        WHERE s.isEnabled = true
          AND s.nextRunAt IS NOT NULL
          AND s.nextRunAt <= :now
    """)
    List<UserDeliverySchedule> findDueSchedules(@Param("now") OffsetDateTime now);

    /**
     * 사용자의 스케줄 조회
     */
    Optional<UserDeliverySchedule> findByUserId(UUID userId);

    /**
     * 사용자의 스케줄 목록 조회 (여러 개인 경우)
     */
    List<UserDeliverySchedule> findAllByUserId(UUID userId);
}