package com.schoolers.service.impl;

import com.schoolers.service.IJwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JwtUtil implements IJwtUtil {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration-millis}")
    private Long expirationMillis;

    @Override
    public String generateToken(String subject, String loginId, String role) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(expirationMillis);

        JwtClaimsSet claimSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiryDate)
                .subject(subject)
                .claim("loginId", loginId)
                .claim("role", role)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimSet))
                .getTokenValue();
    }

    @Override
    public Map<String, Object> validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Instant expiresAt = jwt.getExpiresAt();
            if (!Objects.isNull(expiresAt) && expiresAt.isBefore(Instant.now())) {
                return null;
            }
            return jwt.getClaims();
        } catch (JwtException e) {
            return null;
        }
    }


    @Override
    public String getUserIdFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    @Override
    public String getLoginIdFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Object loginId = jwt.getClaim("loginId");
            return loginId != null ? loginId.toString() : null;
        } catch (JwtException e) {
            return null;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Instant expiresAt = jwt.getExpiresAt();
            return expiresAt == null || expiresAt.isBefore(Instant.now());
        } catch (JwtException e) {
            // if decoding fails, treat as expired/invalid
            return true;
        }
    }

    @Override
    public Long getExpirationMillis() {
        return expirationMillis;
    }
}
