package com.schoolers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomepageResponse {

    private AttendanceStats attendanceStats;
    private List<SessionCard> ongoingSessions;
    private List<SessionCard> upcomingSessions;
    private List<SessionCard> cancelledSessions;
    private List<SessionCard> finishedSessions;
}
