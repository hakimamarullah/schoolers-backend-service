package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.ClockInRequest;
import com.schoolers.dto.response.AttendanceResponse;
import com.schoolers.service.IStudentAttendanceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerJWT")
@LogRequestResponse
@RegisterReflectionForBinding({
        Authentication.class,
        JwtAuthenticationToken.class
})
public class StudentAttendanceController {

    private final IStudentAttendanceService attendanceService;


    @PostMapping("/clock-in")
    @PreAuthorize("#request.loginId == authentication.name")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockIn(@Valid @RequestBody ClockInRequest request) {
        ApiResponse<AttendanceResponse> response = attendanceService.clockIn(request);
        return response.toResponseEntity();
    }

    @GetMapping("/{attendanceId}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendance(@PathVariable Long attendanceId) {
        ApiResponse<AttendanceResponse> response = attendanceService.getAttendanceById(attendanceId);
        return response.toResponseEntity();
    }

    @GetMapping("/session/{sessionId}/student")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getStudentSessionAttendance(@PathVariable Long sessionId,
                                                                                       Authentication authentication) {
        ApiResponse<AttendanceResponse> response = attendanceService.getStudentSessionAttendance(sessionId, authentication.getName());
        return response.toResponseEntity();
    }
}
