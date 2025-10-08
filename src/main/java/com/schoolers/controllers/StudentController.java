package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.service.IStudentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
@LogRequestResponse
@SecurityRequirement(name = "bearerJWT")
@RequiredArgsConstructor
public class StudentController {

    private final IStudentService studentService;

    @PutMapping("/change-classroom/{newClassroomId}")
    public ResponseEntity<ApiResponse<Void>> changeClassroom(@PathVariable Long newClassroomId,
                                                             Authentication authentication) {
        studentService.changeClassroom(newClassroomId, Long.parseLong(authentication.getName()));
        return ResponseEntity.ok(ApiResponse.setSuccess(null));
    }
}
