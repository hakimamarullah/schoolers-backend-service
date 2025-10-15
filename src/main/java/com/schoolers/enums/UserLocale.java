package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum UserLocale {
    EN, ZH, JA, ID;

    @JsonCreator
    public static UserLocale fromValue(String value) {
        return Optional.ofNullable(value)
                .map(String::toUpperCase)
                .map(UserLocale::valueOf)
                .orElse(null);
    }
}
