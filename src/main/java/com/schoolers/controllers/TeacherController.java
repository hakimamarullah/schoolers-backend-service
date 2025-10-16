package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.StartSessionRequest;
import com.schoolers.service.IAttendanceSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teachers")
@LogRequestResponse
@RequiredArgsConstructor
@RolesAllowed({"TEACHER", "OFFICE_ADMIN"})
@SecurityRequirement(name = "bearerJWT")
public class TeacherController {

    private final IAttendanceSessionService attendanceSessionService;

    @PostMapping(value = "/start-session", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> startSession(@RequestBody @Valid StartSessionRequest payload,
                                                            Authentication authentication) {
        payload.setEmployeeNumber(authentication.getName());
        return attendanceSessionService.startSession(payload).toResponseEntity();

    }
}
