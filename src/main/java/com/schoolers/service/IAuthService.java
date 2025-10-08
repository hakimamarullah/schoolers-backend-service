package com.schoolers.service;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.BiometricAuthCompleteRequest;
import com.schoolers.dto.request.BiometricAuthInitRequest;
import com.schoolers.dto.request.BiometricRegistrationRequest;
import com.schoolers.dto.request.LoginRequest;
import com.schoolers.dto.response.AuthResponse;
import com.schoolers.dto.response.BiometricChallengeResponse;
import com.schoolers.dto.response.BiometricRegistrationResponse;
import com.schoolers.models.BiometricCredential;

import java.util.List;

public interface IAuthService {

    ApiResponse<AuthResponse> login(LoginRequest payload);

    ApiResponse<BiometricChallengeResponse> initiateBiometricAuth(BiometricAuthInitRequest payload);

    ApiResponse<AuthResponse> completeBiometricAuth(BiometricAuthCompleteRequest payload);

    ApiResponse<BiometricRegistrationResponse> registerBiometricCredential(BiometricRegistrationRequest payload);

    ApiResponse<List<BiometricCredential>> getUserBiometricCredentials(String loginId);

    ApiResponse<Void> revokeBiometricCredential(String loginId, Long biometricCredentialId);

    ApiResponse<Void> logout(String loginId, String sessionId);
}
