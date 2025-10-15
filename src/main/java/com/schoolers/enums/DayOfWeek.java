package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.format.TextStyle;
import java.util.Locale;
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

    @JsonIgnore
    public String getDisplayName(TextStyle style, Locale locale) {
        return java.time.DayOfWeek.valueOf(this.name()).getDisplayName(style, locale)
                .toUpperCase(locale);
    }
}
