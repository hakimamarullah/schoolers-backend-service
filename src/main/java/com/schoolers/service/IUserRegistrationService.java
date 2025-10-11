package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.RegisterStaffRequest;
import com.schoolers.dto.request.RegisterStudentRequest;
import com.schoolers.dto.response.UserRegistrationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IUserRegistrationService {

    ApiResponse<UserRegistrationResponse> registerStudent(RegisterStudentRequest payload, MultipartFile profilePict);

    ApiResponse<UserRegistrationResponse> registerTeacher(RegisterStaffRequest payload, MultipartFile profilePict);

    ApiResponse<UserRegistrationResponse> registerAdmin(RegisterStaffRequest payload, MultipartFile profilePict);

    ApiResponse<String> updateProfilePicture(String loginId, MultipartFile profilePicture);
}
