package com.schoolers.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartSessionRequest {

    @NotNull(message = "Schedule ID is required")
    private Long scheduleId;

    private String topic;
    private String employeeNumber;
}
