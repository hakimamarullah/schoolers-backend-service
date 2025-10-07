package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;

    @JsonCreator
    public static DayOfWeek fromString(String name) {
        return DayOfWeek.valueOf(name.toUpperCase());
    }
}
