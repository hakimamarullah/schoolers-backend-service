package com.schoolers.dto.request;

import com.schoolers.annotations.Censor;
import com.schoolers.enums.BiometricType;
import com.schoolers.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BiometricRegistrationRequest extends BaseRequest {
    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Device name is required")
    private String deviceName;

    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    @NotNull(message = "Biometric type is required")
    private BiometricType biometricType;

    @NotBlank(message = "Public key is required")
    @Censor
    private String publicKey;

    @NotBlank(message = "Algorithm is required")
    private String algorithm;

    @NotNull(message = "Key size is required")
    private Integer keySize;
}
