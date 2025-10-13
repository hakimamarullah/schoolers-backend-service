package com.schoolers.dto.request;

import com.schoolers.enums.OSType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTokenRequest {


    private String loginId;

    private Long userId;

    private String deviceId;

    private String deviceName;

    @NotBlank(message = "FCM token is required")
    private String token;

    @NotNull(message = "OS type is required")
    private OSType osType;
}
