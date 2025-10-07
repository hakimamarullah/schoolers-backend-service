package com.schoolers.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateSubjectRequest {

    @NotBlank(message = "Name is required")
    @Length(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Subject code is required")
    @Length(max = 20, message = "Subject code must be less than 20 characters")
    private String code;

    @Length(max = 255, message = "Description must be less than 255 characters")
    private String description;
}
