package com.schoolers.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class ClassroomSchedulesInfo {

    private Long classroomId;
    private String classroomName;
    private Map<String, List<ScheduleResponse>> schedules;
}
