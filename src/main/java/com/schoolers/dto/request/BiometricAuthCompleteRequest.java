package com.schoolers.dto.request;

import com.schoolers.annotations.Censor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BiometricAuthCompleteRequest extends BaseRequest {

    @NotBlank(message = "Challenge token is required")
    @Censor
    private String challengeToken;

    @NotBlank(message = "Signed challenge is required")
    private String signedChallenge;

    @NotBlank(message = "Device ID is required")
    private String deviceId;
}
