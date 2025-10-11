package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.projection.StudentAssignmentInfo;
import com.schoolers.dto.response.AssignmentResourceResponse;
import com.schoolers.dto.response.StudentAssignmentResponse;
import com.schoolers.enums.SubmissionStatus;
import com.schoolers.exceptions.DataNotFoundException;
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
import org.springframework.stereotype.Service;

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
        var badge = getRemainingDurationBadge(studentAssignmentInfo.getDueDate());
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

    private StudentAssignmentResponse mapToResponse(StudentAssignmentInfo studentAssignmentInfo) {
        Badge badge = getRemainingDurationBadge(studentAssignmentInfo.getDueDate());
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

    private Badge getRemainingDurationBadge(LocalDateTime dueDate) {
        long minutesDiff = Duration.between(LocalDateTime.now(), dueDate).toMinutes();

        if (minutesDiff < 0) {
            // Overdue
            long overdueMinutes = Math.abs(minutesDiff);
            long days = overdueMinutes / (24 * 60);
            long hours = (overdueMinutes % (24 * 60)) / 60;
            long minutes = overdueMinutes % 60;
            String label = days > 0
                    ? String.format("Overdue by %dd %dh %dm", days, hours, minutes)
                    : String.format("Overdue by %dh %dm", hours, minutes);
            return new Badge("overdue", label);
        }

        // Not overdue yet
        long days = minutesDiff / (24 * 60);
        long hours = (minutesDiff % (24 * 60)) / 60;
        long minutes = minutesDiff % 60;

        if (days > 0) {
            return new Badge("normal", String.format("%dd %dh left", days, hours));
        } else if (hours > 0) {
            return new Badge("urgent", String.format("%dh %dm left", hours, minutes));
        } else {
            return new Badge("urgent", String.format("%dm left", minutes));
        }
    }


    record Badge(String color, String text){}
}
