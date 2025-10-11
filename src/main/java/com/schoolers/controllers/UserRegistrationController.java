package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.RegisterStaffRequest;
import com.schoolers.dto.request.RegisterStudentRequest;
import com.schoolers.dto.response.UserRegistrationResponse;
import com.schoolers.service.impl.UserRegistrationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
@LogRequestResponse
public class UserRegistrationController {

    private final UserRegistrationService userRegistrationService;

    /**
     * POST /api/users/register/student
     * Register new student (requires OFFICE_ADMIN role)
     */
    @PostMapping(value = "/student", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> registerStudent(
            @RequestPart("data") @Valid RegisterStudentRequest request,
            @RequestPart("file") MultipartFile profilePicture) {

        var response = userRegistrationService.registerStudent(request, profilePicture);
        return response.toResponseEntity();
    }


    /**
     * POST /api/users/register/teacher
     * Register new teacher (requires OFFICE_ADMIN role)
     */
    @PostMapping(value = "/teacher", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> registerTeacher(
            @RequestPart("data") @Valid RegisterStaffRequest request,
            @RequestPart("file") MultipartFile profilePicture) {
        var response = userRegistrationService.registerTeacher(request, profilePicture);
        return response.toResponseEntity();
    }

    /**
     * POST /api/users/register/admin
     * Register new office admin (requires OFFICE_ADMIN role)
     */
    @PostMapping(value = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> registerOfficeAdmin(
            @RequestPart("data") RegisterStaffRequest request,
            @RequestPart("file") MultipartFile profilePicture) {
        var response = userRegistrationService.registerAdmin(request, profilePicture);
        return response.toResponseEntity();
    }

    /**
     * PUT /api/users/profile-picture
     * Update own profile picture (requires authentication)
     */
    @PutMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerJWT")
    public ResponseEntity<ApiResponse<String>> updateProfilePicture(
            @RequestParam("file") MultipartFile profilePicture,
            Authentication authentication) {
        var result = userRegistrationService.updateProfilePicture(authentication.getName(), profilePicture);
        return result.toResponseEntity();
    }
}