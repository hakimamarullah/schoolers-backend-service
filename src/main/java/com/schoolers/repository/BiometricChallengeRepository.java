package com.schoolers.repository;

import com.schoolers.enums.ChallengeStatus;
import com.schoolers.models.BiometricChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BiometricChallengeRepository extends JpaRepository<BiometricChallenge, Long> {
    Optional<BiometricChallenge> findByChallengeToken(String challengeToken);
    void deleteByExpiresAtBeforeAndStatus(LocalDateTime dateTime, ChallengeStatus status);
}
