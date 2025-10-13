package com.schoolers.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        JwtClaimsUtils.JwtAuthInfo.class,
        JwtAuthenticationToken.class
})
public class JwtClaimsUtils {

    private final ObjectMapper mapper;

    public Long extractProfileId(JwtAuthenticationToken jwt) {
        return Optional.ofNullable(extractAuthInfo(jwt))
                .map(JwtAuthInfo::getProfileId)
                .orElse(null);
    }

    public JwtAuthInfo extractAuthInfo(JwtAuthenticationToken jwt) {
        return mapper.convertValue(jwt.getToken().getClaims(), JwtAuthInfo.class);
    }

    @Data
    public static class JwtAuthInfo {
        @JsonProperty("sub")
        private String loginId;
        private Long profileId;
        private String role;
        private Long classroomId;
    }

}
