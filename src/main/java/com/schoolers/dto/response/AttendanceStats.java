package com.schoolers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class AttendanceStats {

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;
    private String dayName;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime currentTime;

    private Integer finishedClasses;
    private Integer totalClasses;
}
