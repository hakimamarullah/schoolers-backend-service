package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum OSType {
    IOS,
    ANDROID;

    @JsonCreator
    public static OSType fromValue(String text) {
       return Optional.ofNullable(text)
                .map(OSType::valueOf)
                .orElse(null);
    }
}
