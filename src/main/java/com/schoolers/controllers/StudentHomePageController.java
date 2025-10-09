package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.response.HomepageResponse;
import com.schoolers.service.IStudentHomePageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@LogRequestResponse
@SecurityRequirement(name = "bearerJWT")
public class StudentHomePageController {

    private final IStudentHomePageService homepageService;

    /**
     * Get student homepage with sessions and attendance stats
     *
     * @param date      Optional date parameter (defaults to today)
     * @return Homepage data with ongoing, upcoming, and cancelled sessions
     */
    @GetMapping("/homepage")
    public ResponseEntity<ApiResponse<HomepageResponse>> getStudentHomepage(@RequestParam(required = false)
                                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                            Authentication authentication) {
        return homepageService.getStudentHomepage(authentication.getName(), date).toResponseEntity();
    }
}