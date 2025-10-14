package com.schoolers.listeners;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.schoolers.dto.event.NewInformationEvent;
import com.schoolers.enums.UserRole;
import com.schoolers.repository.DeviceTokenRepository;
import com.schoolers.repository.StudentRepository;
import com.schoolers.repository.UserRepository;
import com.schoolers.service.impl.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class InformationListener {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FCMService fcmService;


    @Async
    @EventListener(NewInformationEvent.class)
    @Retryable(backoff = @Backoff(delay = 5000))
    public void onNewInformation(NewInformationEvent event) throws FirebaseMessagingException {
       log.info("[NEW INFORMATION CREATED] {}", event.getInformationId());
       var userId = new HashSet<>(event.getUserId());
       if (!event.getClassroomId().isEmpty()) {
           userId.addAll(studentRepository.getUserIdByClassroomIdIn(event.getClassroomId()));
       }

       if (!event.getRole().isEmpty()) {
           userId.addAll(userRepository.getUserIdByRoleIn(event.getRole().stream().map(UserRole::valueOf).collect(Collectors.toSet())));
       }
       var tokens = deviceTokenRepository.getAllTokenByUserIdIn(userId);
       BatchResponse response = fcmService.sendToMultipleTokens(new ArrayList<>(tokens), "Hi! You got 1 new information",
               event.getTitle(), Map.of("target", String.format("/info/%d", event.getInformationId())));

       log.info("[END NEW INFORMATION CREATED] {}. {} NOTIFICATIONS SENT", event.getInformationId(), response.getSuccessCount());
    }
}
