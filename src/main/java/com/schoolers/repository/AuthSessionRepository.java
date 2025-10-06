package com.schoolers.repository;

import com.schoolers.models.AuthSession;
import com.schoolers.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    Optional<AuthSession> findBySessionIdAndActiveTrue(String sessionId);
    List<AuthSession> findByUserAndActiveTrue(User user);
    void deleteByExpiresAtBeforeAndActiveTrue(LocalDateTime dateTime);
}
