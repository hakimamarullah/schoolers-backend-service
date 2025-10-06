package com.schoolers.dto.request;

import com.schoolers.annotations.Censor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterStaffRequest {

    @NotBlank(message = "Employee number is required")
    @Censor
    private String employeeNumber; // Will be used as loginId

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Censor
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Censor
    private String password;

    private String gender;
}
