package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.projection.StudentAssignmentInfo;
import com.schoolers.dto.response.AssignmentResourceResponse;
import com.schoolers.dto.response.StudentAssignmentResponse;
import com.schoolers.enums.SubmissionStatus;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.models.StudentAssignment;
import com.schoolers.repository.AssignmentResourceRepository;
import com.schoolers.repository.StudentAssignmentRepository;
import com.schoolers.service.ILocalizationService;
import com.schoolers.service.IStudentAssignmentService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        StudentAssignmentResponse.class,
        StudentAssignmentService.Badge.class,
        PagedResponse.class
})
public class StudentAssignmentService implements IStudentAssignmentService {

    private final StudentAssignmentRepository studentAssignmentRepository;
    private final ILocalizationService localizationService;
    private final AssignmentResourceRepository assignmentResourceRepository;

    @Override
    public ApiResponse<PagedResponse<StudentAssignmentResponse>> getStudentAssigmentByLoginId(String loginId, Pageable pageable) {
        Page<StudentAssignmentResponse> studentAssignments = studentAssignmentRepository.findByStudentStudentNumber(loginId, pageable)
                .map(this::mapToResponse);
        return ApiResponse.setSuccess(PagedResponse.from(studentAssignments));
    }

    @Override
    public ApiResponse<StudentAssignmentResponse> getStudentAssigmentById(Long id, String studentNumber) {
        StudentAssignmentInfo studentAssignmentInfo = studentAssignmentRepository.findByStudentStudentNumberAndId(studentNumber, id)
                .orElseThrow(() -> new DataNotFoundException(localizationService.getMessage("student.assignment-not-found")));

        List<AssignmentResourceResponse> resources = assignmentResourceRepository.findByAssignmentId(studentAssignmentInfo.getParentAssignmentId())
                .stream().map(it -> AssignmentResourceResponse.builder()
                        .resourceName(it.getResourceName())
                        .resourcePath(it.getResourcePath())
                        .resourceType(it.getResourceType())
                        .id(it.getId())
                        .build()).toList();

        var dueDate = studentAssignmentInfo.getDueDate()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm", LocaleContextHolder.getLocale()));
        var badge = getRemainingDurationBadge(studentAssignmentInfo.getDueDate(), studentAssignmentInfo.getCompletedAt());
        var response = StudentAssignmentResponse.builder()
                .parentAssignmentId(studentAssignmentInfo.getParentAssignmentId())
                .status(studentAssignmentInfo.getStatus())
                .id(studentAssignmentInfo.getId())
                .dueDate(dueDate)
                .title(studentAssignmentInfo.getTitle())
                .subjectName(studentAssignmentInfo.getSubjectName())
                .remainingTimeBadgeText(badge.text())
                .badgeColor(badge.color())
                .description(studentAssignmentInfo.getDescription())
                .resources(resources)
                .isSubmitted(Objects.equals(studentAssignmentInfo.getStatus(), SubmissionStatus.SUBMITTED))
                .build();

        return ApiResponse.setSuccess(response);
    }

    @Transactional
    @Override
    public ApiResponse<Void> updateAssignmentStatus(Long assignmentId, String studentNumber, SubmissionStatus submissionStatus) {
        StudentAssignment studentAssignment = studentAssignmentRepository.findByIdAndStudentStudentNumber(assignmentId, studentNumber)
                .orElseThrow(() -> new DataNotFoundException(localizationService.getMessage("student.assignment-not-found")));
        studentAssignment.setStatus(submissionStatus);
        String message = localizationService.getMessage("student.assignment-status-updated");
        if (submissionStatus.equals(SubmissionStatus.SUBMITTED)) {
            studentAssignment.setCompletedAt(LocalDateTime.now());
            message = localizationService.getMessage("student.assignment-submitted",
                    new Object[]{getCompletedBadge(studentAssignment.getCompletedAt(), studentAssignment.getAssignment().getDueDate()).text()});
        }
        return ApiResponse.<Void>builder()
                .code(200)
                .message(message)
                .build();
    }

    private StudentAssignmentResponse mapToResponse(StudentAssignmentInfo studentAssignmentInfo) {
        Badge badge = getRemainingDurationBadge(studentAssignmentInfo.getDueDate(), studentAssignmentInfo.getCompletedAt());
        String dueDate = studentAssignmentInfo.getDueDate()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy", LocaleContextHolder.getLocale()));
        return StudentAssignmentResponse.builder()
                .id(studentAssignmentInfo.getId())
                .title(studentAssignmentInfo.getTitle())
                .dueDate(dueDate)
                .badgeColor(badge.color())
                .parentAssignmentId(studentAssignmentInfo.getParentAssignmentId())
                .remainingTimeBadgeText(badge.text())
                .subjectName(StringUtils.capitalize(studentAssignmentInfo.getSubjectName()))
                .status(studentAssignmentInfo.getStatus())
                .build();
    }

    private static final String LABEL_DONE_ON_TIME = "Done on time";
    private static final String LABEL_DONE_EARLIER = "Done %s earlier";
    private static final String LABEL_DONE_OVERDUE = "Done overdue by %s";
    private static final String LABEL_OVERDUE = "Overdue by %s";
    private static final String LABEL_LEFT = "%s left";

    private static final String BADGE_NORMAL = "normal";
    private static final String BADGE_URGENT = "urgent";
    private static final String BADGE_OVERDUE = "overdue";

    private Badge getRemainingDurationBadge(LocalDateTime dueDate, @Nullable LocalDateTime completedAt) {
        if (completedAt != null) {
            return getCompletedBadge(completedAt, dueDate);
        }

        // Not completed yet
        long minutesDiff = Duration.between(LocalDateTime.now(), dueDate).toMinutes();

        if (minutesDiff < 0) {
            // Overdue
            String label = String.format(LABEL_OVERDUE, formatDuration(Math.abs(minutesDiff)));
            return new Badge(BADGE_OVERDUE, label);
        }

        // Not overdue yet
        if (minutesDiff >= 24 * 60) {
            return new Badge(BADGE_NORMAL, String.format(LABEL_LEFT, formatDuration(minutesDiff)));
        } else if (minutesDiff >= 60) {
            return new Badge(BADGE_URGENT, String.format(LABEL_LEFT, formatDuration(minutesDiff)));
        } else {
            return new Badge(BADGE_URGENT, String.format(LABEL_LEFT, formatDuration(minutesDiff)));
        }
    }

    private Badge getCompletedBadge(LocalDateTime completedAt, LocalDateTime dueDate) {
        long minutesDiff = Duration.between(completedAt, dueDate).toMinutes();

        if (Math.abs(minutesDiff) < 1) {
            // Completed exactly on time (within 1 minute tolerance)
            return new Badge(BADGE_NORMAL, LABEL_DONE_ON_TIME);
        } else if (minutesDiff > 0) {
            // Completed before due date
            String label = String.format(LABEL_DONE_EARLIER, formatDuration(minutesDiff));
            return new Badge(BADGE_NORMAL, label);
        } else {
            // Completed after due date
            String label = String.format(LABEL_DONE_OVERDUE, formatDuration(Math.abs(minutesDiff)));
            return new Badge(BADGE_NORMAL, label);
        }
    }

    /**
     * Helper to format minutes into "Xd Xh Xm" string
     */
    private String formatDuration(long totalMinutes) {
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    record Badge(String color, String text){}
}
