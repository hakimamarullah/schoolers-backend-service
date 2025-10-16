package com.schoolers.listeners;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.schoolers.dto.event.FCMFailedEvent;
import com.schoolers.dto.event.SessionStartEvent;
import com.schoolers.dto.projection.AttendanceSessionInfo;
import com.schoolers.dto.projection.UserToken;
import com.schoolers.dto.request.SendNotificationRequest;
import com.schoolers.repository.AttendanceSessionRepository;
import com.schoolers.repository.DeviceTokenRepository;
import com.schoolers.service.ILocalizationService;
import com.schoolers.service.impl.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        SessionStartEvent.class
})
public class SessionStartListener {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final FCMService fcmService;
    private final DeviceTokenRepository deviceTokenRepository;
    private final ILocalizationService localizationService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener(SessionStartEvent.class)
    @Retryable(backoff = @Backoff(delay = 3000))
    public void onSessionStart(SessionStartEvent event) {
        log.info("[SESSION START] -> {}", event.getSessionId());
        Optional<AttendanceSessionInfo> session = attendanceSessionRepository.getSessionInfoById(event.getSessionId());
        if (session.isEmpty()) {
            return;
        }


        List<UserToken> userTokens = deviceTokenRepository.getAllTokenByClassroomId(session.get().getClassroomId());

        for (UserToken userToken : userTokens) {
            CompletableFuture.runAsync(() -> {
                try {
                    var locale =  Locale.of(userToken.getLocale());
                    var request = SendNotificationRequest.builder()
                            .title(localizationService.getMessageWithLocale("learning.session-started-title", locale))
                            .body(localizationService.getMessageWithLocale("learning.session-started-body", new Object[]{session.get().getSubjectName()}, locale))
                            .token(userToken.getToken())
                            .data(Map.of("target", "/home"))
                            .build();
                    fcmService.sendToToken(request);
                } catch (FirebaseMessagingException ex) {
                    eventPublisher.publishEvent(FCMFailedEvent.builder()
                            .token(userToken.getToken())
                            .errorCode(ex.getMessagingErrorCode())
                            .build());
                } catch (Exception ignore) {
                   // Do Nothing
                }
            });
        }

        log.info("[END BROADCAST SESSION START] -> {}", event.getSessionId());


    }

}
