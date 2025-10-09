package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.ClockInRequest;
import com.schoolers.dto.response.AttendanceResponse;
import org.springframework.transaction.annotation.Transactional;

public interface IStudentAttendanceService {

    /**
     * Student clocks in for attendance
     * @param request Clock in request with student ID, session ID, and location
     * @return ApiResponse with attendance details
     */
    ApiResponse<AttendanceResponse> clockIn(ClockInRequest request);

    /**
     * Get attendance record by ID
     * @param attendanceId Attendance record ID
     * @return ApiResponse with attendance details
     */
    ApiResponse<AttendanceResponse> getAttendanceById(Long attendanceId);


    @Transactional(readOnly = true)
    ApiResponse<AttendanceResponse> getStudentSessionAttendance(Long sessionId, String studentNumber);
}
