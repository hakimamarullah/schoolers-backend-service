package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.response.SubjectInfo;
import com.schoolers.service.ISubjectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@RolesAllowed({"OFFICE_ADMIN", "STUDENT", "TEACHER"})
@LogRequestResponse
@SecurityRequirement(name = "bearerJWT")
public class SubjectController {

    private final ISubjectService subjectService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PagedResponse<SubjectInfo>>> getSubjectList(@ParameterObject Pageable pageable) {
        return subjectService.getSubjectList(pageable).toResponseEntity();
    }

    @GetMapping(value = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SubjectInfo>> getSubjectByCode(@PathVariable String code) {
        return subjectService.getSubjectByCode(code).toResponseEntity();
    }
}
