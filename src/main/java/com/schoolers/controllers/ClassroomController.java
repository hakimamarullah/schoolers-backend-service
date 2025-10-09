package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.GetAttendanceSessionInfo;
import com.schoolers.dto.response.AttendanceSessionInfo;
import com.schoolers.dto.response.ClassroomInfo;
import com.schoolers.dto.response.SimpleClassroomInfo;
import com.schoolers.service.IAttendanceSessionService;
import com.schoolers.service.IClassroomService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@LogRequestResponse
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerJWT")
public class ClassroomController {


    private final IClassroomService classroomService;
    private final IAttendanceSessionService attendanceSessionService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<SimpleClassroomInfo>>> getClassrooms() {
        return classroomService.getClassrooms().toResponseEntity();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ClassroomInfo>> getClassroomById(@PathVariable Long id) {
        return classroomService.getClassroomById(id).toResponseEntity();
    }

    @PostMapping(value = "/sessions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AttendanceSessionInfo>> getSessionByClassroomIdAndStatus(@RequestBody @Valid GetAttendanceSessionInfo payload) {
        return attendanceSessionService.getSessionByClassroomIdAndStatus(payload).toResponseEntity();
    }

}
