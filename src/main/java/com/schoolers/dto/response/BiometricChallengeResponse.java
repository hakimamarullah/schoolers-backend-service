package com.schoolers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.schoolers.annotations.Censor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BiometricChallengeResponse {

    @Censor
    private String challengeToken;

    @Censor
    private Long biometricCredentialId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
}
