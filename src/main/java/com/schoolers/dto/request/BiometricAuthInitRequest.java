package com.schoolers.dto.request;

import com.schoolers.annotations.Censor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BiometricAuthInitRequest extends BaseRequest {

    @NotBlank(message = "Login ID is required")
    @Censor
    private String loginId;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Public key hash is required")
    private String publicKeyHash;
}
