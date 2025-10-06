package com.schoolers.dto.response;

import com.schoolers.annotations.Censor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    @Censor
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String sessionId;
    private UserInfo user;
}
