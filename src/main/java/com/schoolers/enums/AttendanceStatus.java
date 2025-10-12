package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum AttendanceStatus {
    PRESENT, ABSENT, LATE, EXCUSED, SICK;

    @JsonCreator
    public AttendanceStatus fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(AttendanceStatus::valueOf)
                .orElse(null);
    }
}
