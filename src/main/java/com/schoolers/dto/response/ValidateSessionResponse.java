package com.schoolers.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ValidateSessionResponse {

    private Boolean isValid;
}
