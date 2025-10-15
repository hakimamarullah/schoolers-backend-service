package com.schoolers.dto.request;

import com.schoolers.enums.UserLocale;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLocaleRequest {

    @NotNull(message = "Locale is required")
    private UserLocale locale;
}
