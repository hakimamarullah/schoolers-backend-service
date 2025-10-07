package com.schoolers.controllers.admin;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.CreateSubjectRequest;
import com.schoolers.dto.request.UpdateSubjectRequest;
import com.schoolers.dto.response.SubjectInfo;
import com.schoolers.service.ISubjectService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/subjects")
@LogRequestResponse
@RequiredArgsConstructor
@RolesAllowed({"OFFICE_ADMIN"})
public class AdminSubjectController {

    private final ISubjectService subjectService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubjectInfo>> addSubject(@Valid @RequestBody CreateSubjectRequest payload) {
        return subjectService.addSubject(payload).toResponseEntity();
    }

    @PutMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateSubject(@Valid @RequestBody UpdateSubjectRequest payload) {
        subjectService.updateSubject(payload);
        return ApiResponse.<Void>setSuccess(null).toResponseEntity();
    }
}
