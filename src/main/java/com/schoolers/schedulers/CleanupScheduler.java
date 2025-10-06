package com.schoolers.schedulers;

import com.schoolers.enums.ChallengeStatus;
import com.schoolers.repository.AuthSessionRepository;
import com.schoolers.repository.BiometricChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final BiometricChallengeRepository challengeRepository;
    private final AuthSessionRepository sessionRepository;

    /**
     * Clean up expired biometric challenges every 10 minutes
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void cleanupExpiredChallenges() {
        try {
            challengeRepository.deleteByExpiresAtBeforeAndStatus(
                    LocalDateTime.now(),
                    ChallengeStatus.PENDING
            );
            log.info("Cleaned up expired biometric challenges");
        } catch (Exception e) {
            log.error("Error cleaning up challenges", e);
        }
    }

    /**
     * Clean up expired sessions every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredSessions() {
        try {
            sessionRepository.deleteByExpiresAtBeforeAndActiveTrue(LocalDateTime.now());
            log.info("Cleaned up expired sessions");
        } catch (Exception e) {
            log.error("Error cleaning up sessions", e);
        }
    }
}
