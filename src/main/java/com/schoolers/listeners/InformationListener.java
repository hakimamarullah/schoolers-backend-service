package com.schoolers.listeners;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.internal.MessagingServiceErrorResponse;
import com.google.firebase.messaging.internal.MessagingServiceResponse;
import com.schoolers.dto.event.FCMFailedEvent;
import com.schoolers.dto.event.NewInformationEvent;
import com.schoolers.dto.request.SendNotificationRequest;
import com.schoolers.enums.UserRole;
import com.schoolers.repository.DeviceTokenRepository;
import com.schoolers.repository.StudentRepository;
import com.schoolers.repository.UserRepository;
import com.schoolers.service.ILocalizationService;
import com.schoolers.service.impl.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        FirebaseMessaging.class,
        MessagingServiceResponse.class,
        MessagingServiceErrorResponse.class
})
public class InformationListener {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FCMService fcmService;
    private final ILocalizationService localizationService;
    private final ApplicationEventPublisher eventPublisher;

    @Qualifier("taskExecutor")
    private final Executor executor;


    @Async
    @EventListener(NewInformationEvent.class)
    @Retryable(backoff = @Backoff(delay = 5000))
    public void onNewInformation(NewInformationEvent event) {
        log.info("[NEW INFORMATION CREATED] {}", event.getInformationId());
        var userId = new HashSet<>(event.getUserId());
        if (!event.getClassroomId().isEmpty()) {
            userId.addAll(studentRepository.getUserIdByClassroomIdIn(event.getClassroomId()));
        }

        if (!event.getRole().isEmpty()) {
            userId.addAll(userRepository.getUserIdByRoleIn(event.getRole().stream().map(UserRole::valueOf).collect(Collectors.toSet())));
        }
        var tokens = deviceTokenRepository.getAllTokenByUserIdIn(userId);
        var userLocales = userRepository.getAllLocaleByUserIdIn(userId);
        Map<Long, Map<String, Object>> tokensMap = new HashMap<>();
        for (var item : tokens) {
            tokensMap.put(item.getOwnerId(), new HashMap<>(Map.of("token", item.getToken())));
        }

        for (var item : userLocales) {
            if (tokensMap.containsKey(item.getUserId())) {
                tokensMap.get(item.getUserId()).put("locale", item.toLocale());
            }
        }
        try {
            log.info("[NOTIFY NEW INFORMATION CREATED] {}", event.getInformationId());
            CompletableFuture.runAsync(() -> broadCastNotifications(tokensMap, event.getTitle(), event.getInformationId()), executor);
            log.info("[NOTIFY NEW INFORMATION CREATED] {} DONE.", event.getInformationId());
        } catch (Exception e) {
            log.error("Exception Class: {} {}", e.getClass().getCanonicalName(), e.getMessage(), e);
        }
        log.info("[END NEW INFORMATION CREATED] {}", event.getInformationId());

    }


    protected void broadCastNotifications(Map<Long, Map<String, Object>> data, String body, Long informationId) {
        for (var entry : data.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    var request = getNotificationDate(entry.getValue(), body, informationId);
                    if (StringUtils.isNotBlank(request.getToken())) {
                        fcmService.sendToToken(request);
                    }
                } catch (FirebaseMessagingException ex) {
                    eventPublisher.publishEvent(FCMFailedEvent.builder()
                            .token(entry.getValue().getOrDefault("token", "").toString())
                            .errorCode(ex.getMessagingErrorCode())
                            .build());
                } catch (Exception ex) {
                    log.warn("FAILED SEND NOTIF NEW INFORMATION: {}", ex.getMessage(), ex);
                }
            }, executor);
        }
    }

    private SendNotificationRequest getNotificationDate(Map<String, Object> data, String body, Long informationId) {
        return SendNotificationRequest.builder()
                .title(localizationService.getMessageWithLocale("notifications.new-information-title", (Locale) data.getOrDefault("locale", "en")))
                .body(body)
                .token(data.getOrDefault("token", "").toString())
                .data(Map.of("target", String.format("/info/%s", informationId)))
                .build();
    }
}
