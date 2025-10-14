package com.schoolers.dto.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class NewAssignmentEvent {

    private Long assignmentId;

    private Long classroomId;

    private String title;

    private Long subjectId;
}
