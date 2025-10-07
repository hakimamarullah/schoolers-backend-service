package com.schoolers.listeners;

import com.schoolers.dto.event.AuthAttemptEvent;
import com.schoolers.models.AuthAttempt;
import com.schoolers.repository.AuthAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthAttemptListener {

    private final AuthAttemptRepository authAttemptRepository;


    @EventListener(AuthAttemptEvent.class)
    @Async
    @Transactional
    public void onApplicationEvent(AuthAttemptEvent event) {
        log.info("Auth attempt event received: {}", event);
        AuthAttempt attempt = new AuthAttempt();
        attempt.setLoginId(event.getLoginId());
        attempt.setUserId(event.getUserId());
        attempt.setAttemptMethod(event.getMethod());
        attempt.setSuccessful(event.isSuccessful());
        attempt.setFailureReason(event.getFailureReason());
        attempt.setIpAddress(event.getIpAddress());
        attempt.setUserAgent(event.getUserAgent());
        attempt.setDeviceId(event.getDeviceId());
        authAttemptRepository.save(attempt);
        log.info("Auth attempt saved: {}", attempt.getId());
    }
}
