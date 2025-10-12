package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum UserRole {
    STUDENT, TEACHER, OFFICE_ADMIN;

    @JsonCreator
    public UserRole fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(UserRole::valueOf)
                .orElse(null);
    }
}
