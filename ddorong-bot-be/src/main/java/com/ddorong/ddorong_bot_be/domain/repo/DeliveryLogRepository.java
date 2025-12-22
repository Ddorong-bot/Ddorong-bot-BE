package com.ddorong.ddorong_bot_be.domain.repo;

import com.ddorong.ddorong_bot_be.domain.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, UUID> {
    
    @Query("""
        SELECT dl.article.id FROM DeliveryLog dl
        WHERE dl.user.id = :userId
          AND dl.channelType = :channelType
          AND dl.status = 'SENT'
    """)
    List<UUID> findSentArticleIds(@Param("userId") UUID userId, @Param("channelType") String channelType);
}
