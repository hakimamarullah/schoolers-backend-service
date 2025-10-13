package com.schoolers.controllers;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import com.schoolers.annotations.LogRequestResponse;
import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.RegisterTokenRequest;
import com.schoolers.dto.request.SendNotificationRequest;
import com.schoolers.dto.request.SubscribeTopicRequest;
import com.schoolers.models.DeviceToken;
import com.schoolers.service.impl.DeviceTokenService;
import com.schoolers.service.impl.FCMService;
import com.schoolers.utils.JwtClaimsUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@LogRequestResponse
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerJWT")
public class NotificationController {


    private final FCMService fcmService;
    private final DeviceTokenService deviceTokenService;
    private final JwtClaimsUtils jwtClaimsUtils;

    /**
     * Register device token
     */
    @PostMapping("/tokens/register")
    public ResponseEntity<ApiResponse<DeviceToken>> registerToken(
            @Valid @RequestBody RegisterTokenRequest request,
            Authentication authentication,
            JwtAuthenticationToken jwt) {
        request.setLoginId(authentication.getName());
        request.setUserId(jwtClaimsUtils.extractProfileId(jwt));
        DeviceToken token = deviceTokenService.registerToken(request);
        return ApiResponse.<DeviceToken>builder()
                .code(HttpStatus.OK.value())
                .message("Token registered successfully")
                .data(token)
                .build()
                .toResponseEntity();
    }

    /**
     * Register device token with topic subscription
     */
    @PostMapping("/tokens/register-with-topics")
    public ResponseEntity<ApiResponse<DeviceToken>> registerTokenWithTopics(
            @Valid @RequestBody RegisterTokenRequest request,
            @RequestParam List<String> topics,
            Authentication authentication,
            JwtAuthenticationToken jwt) throws FirebaseMessagingException {
        request.setUserId(jwtClaimsUtils.extractProfileId(jwt));
        request.setLoginId(authentication.getName());
        DeviceToken token = deviceTokenService.registerTokenWithTopics(request, topics);
        return ApiResponse.<DeviceToken>builder()
                .code(HttpStatus.OK.value())
                .message("Token registered and subscribed to topics successfully")
                .data(token)
                .build()
                .toResponseEntity();
    }

    /**
     * Refresh device token
     */
    @PutMapping("/tokens/refresh")
    public ResponseEntity<ApiResponse<DeviceToken>> refreshToken(
            @RequestParam String oldToken,
            @Valid @RequestBody RegisterTokenRequest request,
            Authentication authentication,
            JwtAuthenticationToken jwt) {

        request.setLoginId(authentication.getName());
        request.setUserId(jwtClaimsUtils.extractProfileId(jwt));
        DeviceToken token = deviceTokenService.refreshToken(oldToken, request);
        return ApiResponse.<DeviceToken>builder()
                .code(HttpStatus.OK.value())
                .message("Token refreshed successfully")
                .data(token)
                .build()
                .toResponseEntity();
    }

    /**
     * Delete device token
     */
    @DeleteMapping("/tokens/{token}")
    public ResponseEntity<ApiResponse<Void>> deleteToken(@PathVariable String token,
                                                         Authentication authentication) {
        deviceTokenService.deleteToken(token, authentication.getName());
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Token deleted successfully")
                .build()
                .toResponseEntity();
    }

    /**
     * Subscribe token to topic
     */
    @PostMapping("/topics/subscribe")
    public ResponseEntity<ApiResponse<String>> subscribeToTopic(
            @Valid @RequestBody SubscribeTopicRequest request) throws FirebaseMessagingException {

        TopicManagementResponse response = fcmService.subscribeToTopic(request.getToken(), request.getTopic());
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully subscribed to topic: " + request.getTopic())
                .data("Success count: " + response.getSuccessCount())
                .build()
                .toResponseEntity();
    }

    /**
     * Unsubscribe token from topic
     */
    @PostMapping("/topics/unsubscribe")
    public ResponseEntity<ApiResponse<String>> unsubscribeFromTopic(
            @Valid @RequestBody SubscribeTopicRequest request) throws FirebaseMessagingException {

        TopicManagementResponse response = fcmService.unsubscribeFromTopic(request.getToken(), request.getTopic());
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully unsubscribed from topic: " + request.getTopic())
                .data("Success count: " + response.getSuccessCount())
                .build()
                .toResponseEntity();
    }

    /**
     * Send notification to single device
     */
    @PostMapping("/send/token")
    @RolesAllowed({"OFFICE_ADMIN", "TEACHER"})
    public ResponseEntity<ApiResponse<String>> sendToToken(
            @Valid @RequestBody SendNotificationRequest request) throws FirebaseMessagingException {

        String response = fcmService.sendToToken(request);
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Notification sent successfully")
                .data(response)
                .build()
                .toResponseEntity();
    }

    /**
     * Send notification to topic
     */
    @PostMapping("/send/topic")
    @RolesAllowed({"OFFICE_ADMIN", "TEACHER"})
    public ResponseEntity<ApiResponse<String>> sendToTopic(
            @Valid @RequestBody SendNotificationRequest request) throws FirebaseMessagingException {

        String response = fcmService.sendToTopic(request);
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Notification sent to topic successfully")
                .data(response)
                .build()
                .toResponseEntity();
    }

    /**
     * Send notification to user (all devices)
     */
    @PostMapping("/send/user/{userId}")
    @RolesAllowed({"OFFICE_ADMIN", "TEACHER"})
    public ResponseEntity<ApiResponse<String>> sendToUser(
            @PathVariable String userId,
            @Valid @RequestBody SendNotificationRequest request) throws FirebaseMessagingException {

        BatchResponse response = fcmService.sendToUser(userId, request.getTitle(), request.getBody(), request.getData());

        if (response == null) {
            return ApiResponse.<String>builder()
                    .code(HttpStatus.OK.value())
                    .message("No active devices found for user")
                    .build()
                    .toResponseEntity();
        }

        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Notification sent to user's devices")
                .data(String.format("Success: %d, Failure: %d", response.getSuccessCount(), response.getFailureCount()))
                .build()
                .toResponseEntity();
    }

    /**
     * Get user's active tokens
     */
    @GetMapping("/tokens/user/{userId}")
    @RolesAllowed({"OFFICE_ADMIN", "TEACHER"})
    public ResponseEntity<ApiResponse<List<DeviceToken>>> getUserTokens(@PathVariable String userId) {
        List<DeviceToken> tokens = deviceTokenService.getUserActiveTokens(userId);
        return ApiResponse.<List<DeviceToken>>builder()
                .code(HttpStatus.OK.value())
                .message("User tokens retrieved successfully")
                .data(tokens)
                .build()
                .toResponseEntity();
    }
}
