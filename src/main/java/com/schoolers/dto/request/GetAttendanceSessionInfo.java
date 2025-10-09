package com.schoolers.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.schoolers.enums.SessionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GetAttendanceSessionInfo {

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    @NotNull(message = "Status is required")
    private SessionStatus status;

    @NotNull(message = "Session date is required")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate sessionDate;
}
