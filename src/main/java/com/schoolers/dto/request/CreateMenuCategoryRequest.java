package com.schoolers.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateMenuCategoryRequest {

    @NotBlank(message = "Menu category name is required")
    @Length(max = 30, message = "Menu Category name max length 30 chars")
    private String name;
}
