package com.schoolers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateSubjectRequest {

    @NotNull(message = "Subject id is required")
    private Long id;

    @NotNull(message = "Subject name is required")
    @Length(max = 50, message = "Subject name must be less than 50 characters")
    private String name;

    private String description;

    @NotBlank(message = "Subject code is required")
    @Length(max = 20, message = "Subject code must be less than 20 characters")
    private String code;
}
