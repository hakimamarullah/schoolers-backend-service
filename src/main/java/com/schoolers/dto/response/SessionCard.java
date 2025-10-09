package com.schoolers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.schoolers.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionCard {

    private Long sessionId;
    private Long scheduleId;
    private String subjectName;
    private String room;
    private String teacherName;
    private String datetime;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate sessionDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    private String topic;
    private AttendanceInfo attendanceInfo;
    private SessionStatus status;
}
