package com.schoolers.dto.projection;

import java.time.LocalDate;
import java.time.LocalTime;

public interface SimpleAttendanceSessionInfo {

    Long getId();
    Long getClassroomId();
    String getRoom();
    String getSubjectName();
    Long getTeacherId();
    String getTopic();
    LocalTime getStartTime();
    LocalTime getEndTime();
    LocalDate getSessionDate();
}
