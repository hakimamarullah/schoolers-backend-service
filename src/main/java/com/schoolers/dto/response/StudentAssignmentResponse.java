package com.schoolers.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.schoolers.enums.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentAssignmentResponse {

    private Long id;
    private Long parentAssignmentId;
    private String title;


    private String dueDate;

    private String subjectName;

    private String remainingTimeBadgeText;

    private String badgeColor;

    private String description;

    private SubmissionStatus status;

    private Boolean isSubmitted;

    private List<AssignmentResourceResponse> resources;

}
