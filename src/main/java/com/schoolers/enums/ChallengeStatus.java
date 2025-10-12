package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum ChallengeStatus {
    PENDING, VERIFIED, EXPIRED, FAILED;

    @JsonCreator
    public ChallengeStatus fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(ChallengeStatus::valueOf)
                .orElse(null);
    }
}

