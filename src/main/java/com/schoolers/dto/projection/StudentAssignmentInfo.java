package com.schoolers.dto.projection;

import com.schoolers.enums.SubmissionStatus;

import java.time.LocalDateTime;

public interface StudentAssignmentInfo {

    Long getId();
    String getTitle();
    LocalDateTime getDueDate();
    String getSubjectName();

    SubmissionStatus getStatus();

    Long getParentAssignmentId();
    String getDescription();
    LocalDateTime getCompletedAt();
}
