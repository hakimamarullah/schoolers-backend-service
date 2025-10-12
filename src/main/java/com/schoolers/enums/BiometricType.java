package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum BiometricType {
    FINGERPRINT, FACE, IRIS, PALM;

    @JsonCreator
    public BiometricType fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(BiometricType::valueOf)
                .orElse(null);
    }
}
