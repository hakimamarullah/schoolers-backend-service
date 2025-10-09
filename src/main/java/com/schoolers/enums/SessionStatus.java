package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public enum SessionStatus {
    SCHEDULED, ONGOING, COMPLETED, CANCELLED;

    @JsonCreator
    public SessionStatus fromString(String name) {
        if (Objects.isNull(name) || name.isBlank()) {
            return null;
        }
        return SessionStatus.valueOf(name.toUpperCase());
    }
}
