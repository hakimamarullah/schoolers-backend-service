package com.schoolers.service.impl;

import com.schoolers.dto.ApiResponse;
import com.schoolers.dto.request.ValidateSessionRequest;
import com.schoolers.dto.response.ValidateSessionResponse;
import com.schoolers.exceptions.DataNotFoundException;
import com.schoolers.models.AuthSession;
import com.schoolers.repository.AuthSessionRepository;
import com.schoolers.repository.UserRepository;
import com.schoolers.service.ISessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        ValidateSessionRequest.class,
        ValidateSessionResponse.class
})
public class SessionService implements ISessionService {

    private final AuthSessionRepository authSessionRepository;

    private final UserRepository userRepository;

    @Transactional
    @Modifying
    @Override
    public ApiResponse<ValidateSessionResponse> validateSession(ValidateSessionRequest payload) {
        Long userId = userRepository.getUserIdByLoginId(payload.getLoginId())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        AuthSession session = authSessionRepository.findByUserIdAndSessionIdAndDeviceId(userId, payload.getSessionId(), payload.getDeviceId())
                .orElseThrow(() -> new DataNotFoundException("Session not found"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setActive(false);
            session.setRevokedAt(LocalDateTime.now());
            session.setRevokedReason("session expired");
            authSessionRepository.save(session);
            return ApiResponse.setResponse(ValidateSessionResponse.builder().isValid(false).build(),
                    "Session expired and has been revoked", 200);
        }
        return ApiResponse.setResponse(ValidateSessionResponse.builder().isValid(true).build(),
                "Session is valid", 200);
    }
}
