package com.schoolers.service;

import java.util.Map;

public interface IJwtUtil {

    String generateToken(String subject, Long profileId, String role);

    Map<String, Object> validateToken(String token);

    String getUserIdFromToken(String token);

    String getLoginIdFromToken(String token);

    boolean isTokenExpired(String token);

    Long getExpirationMillis();
}
