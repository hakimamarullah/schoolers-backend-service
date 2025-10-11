package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.response.StudentAssignmentResponse;
import com.schoolers.service.IStudentAssignmentService;
import com.schoolers.service.IStudentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final IStudentAssignmentService studentAssignmentService;

    @PutMapping("/change-classroom/{newClassroomId}")
    public ResponseEntity<ApiResponse<Void>> changeClassroom(@PathVariable Long newClassroomId,
                                                             Authentication authentication) {
        studentService.changeClassroom(newClassroomId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.setSuccess(null));
    }

    @GetMapping(value = "/assignments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PagedResponse<StudentAssignmentResponse>>> getStudentAssignmentsByLoginId(@ParameterObject
                                                                                                                @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable,
                                                                                                                Authentication authentication) {
        return studentAssignmentService.getStudentAssigmentByLoginId(authentication.getName(), pageable).toResponseEntity();
    }

    @GetMapping(value = "/assignments/{id}")
    public ResponseEntity<ApiResponse<StudentAssignmentResponse>> getStudentAssignmentById(@PathVariable Long id, Authentication authentication) {
        return studentAssignmentService.getStudentAssigmentById(id, authentication.getName())
                .toResponseEntity();
    }
}
