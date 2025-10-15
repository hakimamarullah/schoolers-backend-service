package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.UpdateLocaleRequest;
import com.schoolers.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@LogRequestResponse
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PatchMapping(value = "/locale", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateLocale(@RequestBody @Valid UpdateLocaleRequest payload,
                                                          Authentication authentication) {
        return userService.updateLocale(payload.getLocale(), authentication.getName()).toResponseEntity();
    }
}
