package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum AuthMethod {
    PASSWORD, BIOMETRIC;

    @JsonCreator
    public AuthMethod fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(AuthMethod::valueOf)
                .orElse(null);
    }
}
