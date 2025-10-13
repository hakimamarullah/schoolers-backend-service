package com.schoolers.service.impl;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.schoolers.dto.request.RegisterTokenRequest;
import com.schoolers.enums.OSType;
import com.schoolers.models.DeviceToken;
import com.schoolers.models.User;
import com.schoolers.repository.DeviceTokenRepository;
import com.schoolers.service.IDeviceTokenService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@RegisterReflectionForBinding({
        DeviceToken.class,
        RegisterTokenRequest.class,
        OSType.class
})
public class DeviceTokenService implements IDeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final FCMService fcmService;
    private final EntityManager entityManager;

    /**
     * Register or update device token
     */
    @Transactional
    public DeviceToken registerToken(RegisterTokenRequest request) {
        return registerTokenWrapper(request);
    }

    protected DeviceToken registerTokenWrapper(RegisterTokenRequest request) {
        Optional<DeviceToken> existingToken = deviceTokenRepository
                .findByUserLoginIdAndTokenAndOsType(request.getLoginId(), request.getToken(), request.getOsType());

        if (existingToken.isPresent()) {
            return existingToken.get();
        }

        DeviceToken newToken = DeviceToken.builder()
                .user(entityManager.getReference(User.class, request.getUserId()))
                .token(request.getToken())
                .osType(request.getOsType())
                .deviceId(request.getDeviceId())
                .deviceName(request.getDeviceName())
                .active(true)
                .build();

        log.info("Registered new token for user: {} with OS: {}", request.getLoginId(), request.getOsType());
        return deviceTokenRepository.save(newToken);
    }

    /**
     * Register token and subscribe to topics
     */
    @Transactional
    public DeviceToken registerTokenWithTopics(RegisterTokenRequest request, List<String> topics)
            throws FirebaseMessagingException {

        DeviceToken deviceToken = registerTokenWrapper(request);

        // Subscribe to topics
        if (topics != null && !topics.isEmpty()) {
            for (String topic : topics) {
                fcmService.subscribeToTopic(request.getToken(), topic);
                log.info("Subscribed token to topic: {}", topic);
            }
        }

        return deviceToken;
    }

    /**
     * Refresh token (update existing token)
     */
    @Transactional
    public DeviceToken refreshToken(String oldToken, RegisterTokenRequest request) {
        // Deactivate old token
        deviceTokenRepository.findByToken(oldToken).ifPresent(token -> {
            token.setActive(false);
            deviceTokenRepository.saveAndFlush(token);
            log.info("Deactivated old token");
        });

        // Register new token
        return registerTokenWrapper(request);
    }

    /**
     * Delete token
     */
    @Transactional
    public void deleteToken(String token, String loginId) {
        deviceTokenRepository.deleteByUserLoginIdAndToken(loginId, token);
        log.info("Deleted token: {}", token);
    }

    /**
     * Get all active tokens for a user
     */
    public List<DeviceToken> getUserActiveTokens(String loginId) {
        return deviceTokenRepository.findByUserLoginIdAndActive(loginId, true);
    }

    /**
     * Deactivate all tokens for a user
     */
    @Transactional
    public void deactivateUserTokens(String userId) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserLoginId(userId);
        tokens.forEach(token -> token.setActive(false));
        deviceTokenRepository.saveAll(tokens);
        log.info("Deactivated all tokens for user: {}", userId);
    }
}
