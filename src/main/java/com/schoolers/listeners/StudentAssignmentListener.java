package com.schoolers.listeners;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.schoolers.dto.event.FCMFailedEvent;
import com.schoolers.dto.event.NewAssignmentEvent;
import com.schoolers.dto.projection.UserLocale;
import com.schoolers.dto.projection.UserToken;
import com.schoolers.dto.request.SendNotificationRequest;
import com.schoolers.enums.SubmissionStatus;
import com.schoolers.models.Assignment;
import com.schoolers.models.Student;
import com.schoolers.models.StudentAssignment;
import com.schoolers.repository.DeviceTokenRepository;
import com.schoolers.repository.StudentAssignmentRepository;
import com.schoolers.repository.StudentRepository;
import com.schoolers.repository.SubjectRepository;
import com.schoolers.repository.UserRepository;
import com.schoolers.service.ILocalizationService;
import com.schoolers.service.impl.FCMService;
import jakarta.persistence.EntityManager;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
@Slf4j
@RegisterReflectionForBinding(classNames = {
        "com.google.firebase.messaging.FirebaseMessagingClientImpl"
})
public class StudentAssignmentListener {

    public static final String TOKEN_KEY = "token";
    private final StudentAssignmentRepository studentAssignmentRepository;

    private final StudentRepository studentRepository;

    private final DeviceTokenRepository deviceTokenRepository;

    private final SubjectRepository subjectRepository;

    private final EntityManager entityManager;

    private final UserRepository userRepository;

    private final FCMService fcmService;

    private final ILocalizationService localizationService;

    private final ApplicationEventPublisher eventPublisher;

    @Qualifier("taskExecutor")
    private final Executor executor;

    @Async
    @Transactional
    @EventListener(NewAssignmentEvent.class)
    @Retryable(backoff = @Backoff(delay = 3000))
    public void onNewAssignment(NewAssignmentEvent event) {
        log.info("[NEW ASSIGNMENT] Classroom: {} Assignment: {}", event.getClassroomId(), event.getAssignmentId());
        var assignment = entityManager.getReference(Assignment.class, event.getAssignmentId());
        var studentAssignments = studentRepository.findAllIdByClassroomId(event.getClassroomId())
                .stream().map(it -> {
                    StudentAssignment studentAssignment = new StudentAssignment();
                    studentAssignment.setAssignment(assignment);
                    studentAssignment.setStudent(entityManager.getReference(Student.class, it));
                    studentAssignment.setStatus(SubmissionStatus.NOT_SUBMITTED);
                    return studentAssignment;
                }).toList();
        if (studentAssignments.isEmpty()) {
            return;
        }
        List<StudentAssignment> assignments = studentAssignmentRepository.saveAll(studentAssignments);
        Map<Long, Map<String, Object>> tokens = new HashMap<>();
        for (var item : assignments) {
            Map<String, Object> data = new HashMap<>();
            data.put("assignmentId", item.getId());
            tokens.put(item.getStudent().getId(), data);
        }
        List<UserToken> userTokens = deviceTokenRepository.getUserTokenByStudentIdIn(tokens.keySet());
        List<UserLocale> userLocales = userRepository.getAllLocaleByStudentIdIn(tokens.keySet());
        for (var item : userTokens) {
            tokens.get(item.getOwnerId()).put(TOKEN_KEY, item.getToken());
        }

        for (var locale : userLocales) {
            tokens.get(locale.getUserId()).put("locale", locale.toLocale());
        }
        var subjectName = subjectRepository.getNameById(event.getSubjectId()).orElse("");
        CompletableFuture.runAsync(() -> broadCastNotification(tokens, StringUtils.capitalize(subjectName), event.getTitle()), executor);
        log.info("[NEW ASSIGNMENT] Student assignments created successfully. Count: {}", studentAssignments.size());
    }


    protected void broadCastNotification(Map<Long, Map<String, Object>> data, String subjectName, String body) {
        log.info("[BROADCAST NOTIFICATION] Sending notifications to {} users", data.size());
        for (var entry : data.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    var request = getNotificationData(entry.getValue(), subjectName, body);
                    if (StringUtils.isNotBlank(request.getToken())) {
                        fcmService.sendToToken(request);
                    }
                } catch (FirebaseMessagingException ex) {
                    eventPublisher.publishEvent(FCMFailedEvent.builder()
                            .token(entry.getValue().getOrDefault(TOKEN_KEY, "").toString())
                            .errorCode(ex.getMessagingErrorCode())
                            .build());
                } catch (Exception ex) {
                    log.error("Failed to send notification to: {}", entry.getValue(), ex);
                }
            }, executor);
        }
    }

    private SendNotificationRequest getNotificationData(Map<String, Object> itemData, String subjectName, String body) {
        return SendNotificationRequest.builder()
                .title(localizationService.getMessageWithLocale("notifications.new-assignment-title", new Object[]{subjectName}, (Locale) itemData.get("locale")))
                .body(body)
                .token(itemData.getOrDefault(TOKEN_KEY, "").toString())
                .data(Map.of("target", String.format("/assignments/%s", itemData.get("assignmentId"))))
                .build();
    }


}
