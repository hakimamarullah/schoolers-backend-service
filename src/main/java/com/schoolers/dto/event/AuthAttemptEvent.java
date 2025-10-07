package com.schoolers.dto.event;

import com.schoolers.enums.AuthMethod;
import com.schoolers.enums.FailureReason;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AuthAttemptEvent {

    private final String loginId;
    private final Long userId;
    private final AuthMethod method;
    private final boolean successful;
    private final FailureReason failureReason;
    private final String ipAddress;
    private final String userAgent;
    private final String deviceId;
}
