package com.schoolers.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class SubjectInfo {

    private Long id;
    private String name;
    private String description;
    private String code;
}
