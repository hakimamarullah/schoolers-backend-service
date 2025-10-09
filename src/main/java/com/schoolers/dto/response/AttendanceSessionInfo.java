package com.schoolers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder(toBuilder = true)
public class AttendanceSessionInfo {

    private Long attendanceSessionId;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String topic;
    private String subjectName;
    private String room;

    private String teacherName;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate sessionDate;
}
