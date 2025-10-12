package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;

    @JsonCreator
    public DayOfWeek fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .orElse(null);
    }
}
