package com.schoolers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.schoolers.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponse {
    private Long id;
    private Long studentId;
    private Long sessionId;
    private AttendanceStatus status;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime clockInTime;
    private String latitude;
    private String longitude;
    private Boolean isLate;
}
