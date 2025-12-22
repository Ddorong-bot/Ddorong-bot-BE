package com.ddorong.ddorong_bot_be.domain.repo;

import com.ddorong.ddorong_bot_be.domain.UserDeliverySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface UserDeliveryScheduleRepository extends JpaRepository<UserDeliverySchedule, UUID> {
    
    @Query("""
        SELECT s FROM UserDeliverySchedule s
        WHERE s.isEnabled = true
          AND s.nextRunAt IS NOT NULL
          AND s.nextRunAt <= :now
    """)
    List<UserDeliverySchedule> findDueSchedules(@Param("now") OffsetDateTime now);
}
