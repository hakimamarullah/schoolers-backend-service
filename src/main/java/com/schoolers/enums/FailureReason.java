package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum FailureReason {
    INVALID_CREDENTIALS,
    INVALID_SIGNATURE,
    EXPIRED_CHALLENGE,
    USER_NOT_FOUND,
    USER_INACTIVE,
    BIOMETRIC_NOT_ENABLED,
    DEVICE_NOT_REGISTERED,
    ACCOUNT_LOCKED,
    RATE_LIMITED,
    INVALID_TOKEN;

    @JsonCreator
    public FailureReason fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(FailureReason::valueOf)
                .orElse(null);
    }
}
