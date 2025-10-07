package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.BiometricAuthCompleteRequest;
import com.schoolers.dto.request.BiometricAuthInitRequest;
import com.schoolers.dto.request.BiometricRegistrationRequest;
import com.schoolers.dto.request.LoginRequest;
import com.schoolers.dto.response.AuthResponse;
import com.schoolers.dto.response.BiometricChallengeResponse;
import com.schoolers.dto.response.BiometricRegistrationResponse;
import com.schoolers.models.BiometricCredential;
import com.schoolers.service.IAuthService;
import com.schoolers.utils.RequestInfoUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@LogRequestResponse
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    private final RequestInfoUtils requestInfoUtils;

    /**
     * POST /api/auth/login
     * Traditional password login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        requestInfoUtils.injectInfo(request, httpRequest);
        var response = authService.login(request);
        return response.toResponseEntity();
    }

    /**
     * POST /api/auth/biometric/init
     * Step 1: Initiate biometric authentication - get challenge
     */
    @PostMapping("/biometric/init")
    public ResponseEntity<ApiResponse<BiometricChallengeResponse>> initBiometric(
            @Valid @RequestBody BiometricAuthInitRequest request,
            HttpServletRequest httpRequest) {
        requestInfoUtils.injectInfo(request, httpRequest);
        var response = authService.initiateBiometricAuth(request);
        return response.toResponseEntity();
    }

    /**
     * POST /api/auth/biometric/complete
     * Step 2: Complete biometric authentication - verify signed challenge
     */
    @PostMapping("/biometric/complete")
    public ResponseEntity<ApiResponse<AuthResponse>> completeBiometric(
            @Valid @RequestBody BiometricAuthCompleteRequest request,
            HttpServletRequest httpRequest) {
        requestInfoUtils.injectInfo(request, httpRequest);
        var response = authService.completeBiometricAuth(request);
        return response.toResponseEntity();
    }

    /**
     * POST /api/auth/biometric/register
     * Register new biometric credential (requires authentication)
     */
    @PostMapping("/biometric/register")
    public ResponseEntity<ApiResponse<BiometricRegistrationResponse>> registerBiometric(
            @Valid @RequestBody BiometricRegistrationRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        request.setUserId(userId);
        var response = authService.registerBiometricCredential(request);
        return response.toResponseEntity();
    }

    /**
     * GET /api/auth/biometric/credentials
     * Get user's biometric credentials (requires authentication)
     */
    @GetMapping("/biometric/credentials")
    public ResponseEntity<ApiResponse<List<BiometricCredential>>> getBiometricCredentials(
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        var credentials = authService.getUserBiometricCredentials(userId);
        return credentials.toResponseEntity();
    }

    /**
     * DELETE /api/auth/biometric/{credentialId}
     * Revoke biometric credential (requires authentication)
     */
    @DeleteMapping("/biometric/{credentialId}")
    public ResponseEntity<ApiResponse<Void>> revokeBiometric(
            @PathVariable Long credentialId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        var response = authService.revokeBiometricCredential(userId, credentialId);
        return response.toResponseEntity();
    }

    /**
     * POST /api/auth/logout
     * Logout and revoke session
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestParam String sessionId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        var response = authService.logout(userId, sessionId);
        return response.toResponseEntity();
    }
}
