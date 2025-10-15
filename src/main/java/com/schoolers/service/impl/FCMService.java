package com.schoolers.service.impl;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;
import com.schoolers.dto.event.FCMBatchFailedEvent;
import com.schoolers.dto.request.SendNotificationRequest;
import com.schoolers.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@RegisterReflectionForBinding({
        SendNotificationRequest.class,
        FCMBatchFailedEvent.class,
        Message.class,
        MulticastMessage.class,
        Notification.class,
        TopicManagementResponse.class,
        BatchResponse.class,
})
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Send notification to a single device by token
     */
    public String sendToToken(SendNotificationRequest request) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(request.getToken())
                .setNotification(Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .build())
                .putAllData(request.getData() != null ? request.getData() : Map.of())
                        .build();

        String response = firebaseMessaging.send(message);
        log.info("Successfully sent message to token: {}", response);
        return response;
    }

    /**
     * Send notification to a topic
     */
    public String sendToTopic(SendNotificationRequest request) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setTopic(request.getTopic())
                .setNotification(Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .build())
                .putAllData(request.getData() != null ? request.getData() : Map.of())
                .build();

        String response = firebaseMessaging.send(message);
        log.info("Successfully sent message to topic {}: {}", request.getTopic(), response);
        return response;
    }

    /**
     * Send notification to multiple devices
     */
    public BatchResponse sendToMultipleTokens(List<String> tokens, String title, String body, Map<String, String> data)
            throws FirebaseMessagingException {

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data != null ? data : Map.of())
                .build();

        BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
        log.info("Successfully sent messages. Success: {}, Failure: {}",
                response.getSuccessCount(), response.getFailureCount());

        // Handle failed tokens
        if (response.getFailureCount() > 0) {
            eventPublisher.publishEvent(FCMBatchFailedEvent.builder()
                    .batchResponse(response)
                    .tokens(tokens)
                    .build());
        }

        return response;
    }

    /**
     * Subscribe token(s) to topic
     */
    public TopicManagementResponse subscribeToTopic(List<String> tokens, String topic) throws FirebaseMessagingException {
        TopicManagementResponse response = firebaseMessaging.subscribeToTopic(tokens, topic);

        log.info("Successfully subscribed {} tokens to topic {}: Success={}, Failure={}",
                tokens.size(), topic, response.getSuccessCount(), response.getFailureCount());

        return response;
    }

    public TopicManagementResponse subscribeToTopic(String token, String topic) throws FirebaseMessagingException {
        return subscribeToTopic(List.of(token), topic);
    }

    /**
     * Unsubscribe token(s) from topic
     */
    public TopicManagementResponse unsubscribeFromTopic(List<String> tokens, String topic) throws FirebaseMessagingException {
        TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(tokens, topic);
        log.info("Successfully unsubscribed {} tokens from topic {}", tokens.size(), topic);
        return response;
    }

    public TopicManagementResponse unsubscribeFromTopic(String token, String topic) throws FirebaseMessagingException {
        return unsubscribeFromTopic(List.of(token), topic);
    }

    /**
     * Send notification to all devices of a user
     */
    @Transactional
    public BatchResponse sendToUser(String userId, String title, String body, Map<String, String> data)
            throws FirebaseMessagingException {

        List<String> userTokens = deviceTokenRepository.getTokenUserLoginIdAndActive(userId, true);

        if (userTokens.isEmpty()) {
            log.warn("No active tokens found for user: {}", userId);
            return null;
        }

        return sendToMultipleTokens(userTokens, title, body, data);
    }
}
