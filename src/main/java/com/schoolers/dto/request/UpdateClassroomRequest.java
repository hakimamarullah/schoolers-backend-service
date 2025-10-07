package com.schoolers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateClassroomRequest {

    @NotNull(message = "Classroom id is required")
    private Long id;

    @NotBlank(message = "Classroom name is required")
    @Length(max = 20, message = "Classroom name must be less than 20 characters")
    private String name;

    @NotBlank(message = "Classroom grade is required")
    @Length(max = 3, message = "Classroom grade must be less than 3 characters")
    private String grade;

    @NotBlank(message = "Academic year is required")
    @Length(max = 15, message = "Academic year must be less than 15 characters")
    private String academicYear;
}
