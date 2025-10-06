package com.schoolers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.schoolers.annotations.Censor;
import com.schoolers.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserRegistrationResponse {

    private Long userId;

    @Censor
    private Long profileId; // Student ID, Teacher ID, or null for admin

    @Censor
    private String loginId;
    private String fullName;

    @Censor
    private String email;
    private UserRole role;
    private String profilePictureUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
