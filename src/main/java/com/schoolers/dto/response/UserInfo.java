package com.schoolers.dto.response;

import com.schoolers.annotations.Censor;
import com.schoolers.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class UserInfo {

    private Long id;

    @Censor
    private String loginId;
    private String fullName;

    private String profilePictUri;

    private String schoolName;

    private String className;

    private Long classroomId;

    private String grade;

    private String gender;

    @Censor
    private String email;
    private UserRole role;
    private Boolean biometricEnabled;
}
