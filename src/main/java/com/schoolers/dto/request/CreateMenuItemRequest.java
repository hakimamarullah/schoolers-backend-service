package com.schoolers.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateMenuItemRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Title is required")
    @Length(max = 20)
    private String title;

    private String badgeText;

    @NotBlank(message = "Icon is required")
    @Length(max = 20)
    private String icon;

    private String target;

    private Integer ordinal = 0;

    private Boolean enabled = false;
}
