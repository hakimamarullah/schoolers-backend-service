package com.schoolers.dto.projection;

import java.time.LocalTime;

public interface SimpleScheduleInfo {

    Long getId();
    Long getClassroomId();
    Long getTeacherId();
    Long getSubjectId();
    LocalTime getStartTime();
    LocalTime getEndTime();
    Boolean isActive();
}
