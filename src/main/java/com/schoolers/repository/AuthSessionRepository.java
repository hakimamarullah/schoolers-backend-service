package com.schoolers.repository;

import com.schoolers.models.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findBySessionIdAndActiveTrue(String sessionId);

    Optional<AuthSession> findByUserIdAndSessionIdAndDeviceId(Long userId, String sessionId, String deviceId);

    Long deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}
