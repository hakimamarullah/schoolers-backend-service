package com.schoolers.listeners;

import com.google.firebase.messaging.MessagingErrorCode;
import com.schoolers.dto.event.FCMBatchFailedEvent;
import com.schoolers.dto.event.FCMFailedEvent;
import com.schoolers.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        FCMFailedEvent.class
})
public class FailedFCMTokenListener {

    private final DeviceTokenRepository deviceTokenRepository;

    @Async
    @Transactional
    @EventListener(FCMBatchFailedEvent.class)
    public void onBatchFailedToken(FCMBatchFailedEvent event) {
        log.info("[FAILED TOKEN] {}", event.getBatchResponse().getFailureCount());

        var responses = event.getBatchResponse().getResponses();
        var tokens = event.getTokens();

        Set<String> deactivateTokens = IntStream.range(0, responses.size())
                .filter(i -> {
                    var r = responses.get(i);
                    if (r.isSuccessful()) return false;
                    var code = r.getException().getMessagingErrorCode();
                    return code == MessagingErrorCode.INVALID_ARGUMENT || code == MessagingErrorCode.UNREGISTERED;
                })
                .mapToObj(tokens::get)
                .collect(Collectors.toSet());


        int count = deviceTokenRepository.updateByTokenInSetActive(deactivateTokens, false);

        log.info("[END FAILED TOKEN] {}/{} failed tokens has been deactivated", count, event.getBatchResponse().getFailureCount());
    }

    @Async
    @Transactional
    @EventListener(FCMFailedEvent.class)
    @Retryable(backoff = @Backoff(delay = 3000))
    public void onFailedToken(FCMFailedEvent event) {
        log.info("RECEIVED FAILED TOKEN {}", event.getErrorCode().name());
       if (StringUtils.isBlank(event.getToken())) {
           return;
       }
       var code = event.getErrorCode();
       if (code == MessagingErrorCode.INVALID_ARGUMENT || code == MessagingErrorCode.UNREGISTERED) {
           deviceTokenRepository.updateByTokenInSetActive(Set.of(event.getToken()), false);
       }
    }
}
