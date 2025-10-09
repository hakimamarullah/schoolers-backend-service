package com.schoolers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceInfo {
    private Integer attendedSessions;  // How many sessions student attended for this subject
    private Integer totalSessions;     // Total sessions held for this subject
    private Boolean hasClocked;        // Has student clocked in for THIS session
    private String clockInTime;        // Clock in time for THIS session
}
