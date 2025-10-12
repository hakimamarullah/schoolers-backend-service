package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum SessionStatus {
    SCHEDULED, ONGOING, COMPLETED, CANCELLED;

    @JsonCreator
    public SessionStatus fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(SessionStatus::valueOf)
                .orElse(null);
    }
}
