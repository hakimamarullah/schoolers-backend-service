package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.ValidateSessionRequest;
import com.schoolers.dto.response.ValidateSessionResponse;
import com.schoolers.service.ISessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/sessions")
@LogRequestResponse
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerJWT")
public class SessionController {

    private final ISessionService sessionService;

    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ValidateSessionResponse>> validateSession(@RequestBody @Valid ValidateSessionRequest payload,
                                                                                Authentication authentication) {

        payload.setLoginId(authentication.getName());
        return sessionService.validateSession(payload).toResponseEntity();
    }
}
