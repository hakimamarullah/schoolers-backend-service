package com.schoolers.listeners;

import com.schoolers.dto.event.NewAssignmentEvent;
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
import com.schoolers.service.impl.FCMService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudentAssignmentListener {

    private final StudentAssignmentRepository studentAssignmentRepository;

    private final StudentRepository studentRepository;

    private final DeviceTokenRepository deviceTokenRepository;

    private final SubjectRepository subjectRepository;

    private final EntityManager entityManager;

    private final FCMService fcmService;

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
        for (var item : userTokens) {
            tokens.get(item.getOwnerId()).put("token", item.getToken());
        }
        var subjectName = subjectRepository.getNameById(event.getSubjectId()).orElse("");
        var headerTitle = String.format("[%s] dropped a new assignment! ✨ Time to shine, Comrade!",
                StringUtils.capitalize(subjectName.toLowerCase()));
        CompletableFuture.runAsync(() -> broadCastNotification(tokens, headerTitle, event.getTitle()));
        log.info("[NEW ASSIGNMENT] Student assignments created successfully. Count: {}", studentAssignments.size());
    }


    protected void broadCastNotification(Map<Long, Map<String, Object>> data, String title, String body) {
        log.info("[BROADCAST NOTIFICATION] Sending notifications to {}", data);
        for (var entry : data.entrySet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    fcmService.sendToToken(getNotificationData(entry.getValue(), title, body));
                } catch (Exception ignore) {
                    // Do Nothing
                    log.info("Failed to send notification to: {}", entry.getValue());
                }
            });
        }
    }

    private SendNotificationRequest getNotificationData(Map<String, Object> itemData, String title, String body) {
        return SendNotificationRequest.builder()
                .title(title)
                .body(body)
                .token(itemData.getOrDefault("token", "").toString())
                .data(Map.of("target", String.format("/assignments/%s", itemData.get("assignmentId"))))
                .build();
    }
}
