package com.ddorong.ddorong_bot_be.domain.repo;

import com.ddorong.ddorong_bot_be.domain.UserChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserChannelRepository extends JpaRepository<UserChannel, UUID> {
    List<UserChannel> findByUserIdAndIsEnabledTrue(UUID userId);
}
