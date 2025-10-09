package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.GetAttendanceSessionInfo;
import com.schoolers.dto.response.AttendanceSessionInfo;

public interface IAttendanceSessionService {

    ApiResponse<AttendanceSessionInfo> getSessionByClassroomIdAndStatus(GetAttendanceSessionInfo payload);
}
