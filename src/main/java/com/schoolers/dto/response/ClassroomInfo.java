package com.schoolers.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ClassroomInfo {

    private Long id;
    private String name;
    private String grade;
    private String academicYear;
}
