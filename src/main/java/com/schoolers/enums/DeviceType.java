package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum DeviceType {
    MOBILE, TABLET, BIOMETRIC_SCANNER, DESKTOP;

    @JsonCreator
    public DeviceType fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(DeviceType::valueOf)
                .orElse(null);
    }
}
