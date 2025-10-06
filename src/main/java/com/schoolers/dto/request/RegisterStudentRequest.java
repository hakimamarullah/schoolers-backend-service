package com.schoolers.dto.request;

import com.schoolers.annotations.Censor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterStudentRequest {

    @NotBlank(message = "Student number (NISN) is required")
    @Censor
    private String studentNumber; // Will be used as loginId

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    @Censor
    private String email;

    @NotNull(message = "Classroom is required")
    private Long classroomId;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Censor
    private String password;

    private String gender;


}
