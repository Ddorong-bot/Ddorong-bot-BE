package com.ddorong.ddorong_bot_be.domain.repo;

import com.ddorong.ddorong_bot_be.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByCode(String code);
}
