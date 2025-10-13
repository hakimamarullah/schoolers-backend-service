package com.schoolers.controllers;

import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.PagedResponse;
import com.schoolers.dto.request.CreateInformationRequest;
import com.schoolers.dto.response.InformationDetailResponse;
import com.schoolers.dto.response.InformationSimpleResponse;
import com.schoolers.service.impl.InformationService;
import com.schoolers.utils.JwtClaimsUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/informations")
@LogRequestResponse
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerJWT")
public class InformationController {

    private final InformationService informationService;
    private final JwtClaimsUtils jwtClaimsUtils;

    @PostMapping
    @RolesAllowed({"OFFICE_ADMIN", "TEACHER"})
    public ResponseEntity<ApiResponse<InformationSimpleResponse>> createInformation(
            @RequestBody CreateInformationRequest request,
            Authentication authentication) {

        var information = informationService.createInformation(request, authentication.getName());
        return ApiResponse.setResponse(information, 201).toResponseEntity();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<InformationSimpleResponse>>> getInformationList(
            @ParameterObject @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            JwtAuthenticationToken jwt) {

        var user = jwtClaimsUtils.extractAuthInfo(jwt);
        Page<InformationSimpleResponse> result = informationService.getInformationList(
                user.getProfileId(), String.valueOf(user.getClassroomId()), user.getRole(), pageable
        );

        return ApiResponse.setSuccess(PagedResponse.from(result)).toResponseEntity();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InformationDetailResponse>> getInformationDetail(
            @PathVariable Long id,
            JwtAuthenticationToken jwt) throws ExecutionException, InterruptedException {

        var user = jwtClaimsUtils.extractAuthInfo(jwt);
        // Check access
        if (informationService.notHasAccess(id, user.getProfileId(), String.valueOf(user.getClassroomId()), user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        InformationDetailResponse detail = informationService.getInformationDetail(id, user.getProfileId());
        return ApiResponse.setSuccess(detail).toResponseEntity();
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            JwtAuthenticationToken jwt) throws ExecutionException, InterruptedException {

        var user = jwtClaimsUtils.extractAuthInfo(jwt);

        if (informationService.notHasAccess(id, user.getProfileId(), String.valueOf(user.getClassroomId()), user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        informationService.markAsRead(id, user.getProfileId());
        return ApiResponse.<Void>setSuccess(null).toResponseEntity();
    }

    @GetMapping("/count-unread")
    public ResponseEntity<ApiResponse<Long>> countUnreadInformation(JwtAuthenticationToken jwt) {
        var user = jwtClaimsUtils.extractAuthInfo(jwt);
        Long count = informationService.countUnreadInformation(user.getProfileId(), String.valueOf(user.getClassroomId()), user.getRole());
        return ApiResponse.setSuccess(count).toResponseEntity();
    }
}
