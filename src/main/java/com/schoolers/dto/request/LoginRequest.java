package com.schoolers.dto.request;

import com.schoolers.annotations.Censor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginRequest extends BaseRequest {

    @NotBlank(message = "Login ID is required")
    @Censor
    private String loginId;

    @NotBlank(message = "Password is required")
    @Censor
    private String password;

    private String deviceId;
    private String deviceName;
}
