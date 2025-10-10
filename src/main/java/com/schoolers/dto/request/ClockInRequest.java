package com.schoolers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClockInRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotBlank(message = "Latitude is required")
    private String latitude;

    @NotBlank(message = "Longitude is required")
    private String longitude;

    @NotBlank(message = "Login ID is required")
    private String loginId;
}
