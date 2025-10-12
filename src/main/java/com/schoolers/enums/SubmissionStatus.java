package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum SubmissionStatus {

    LATE,
    SUBMITTED,
    NOT_SUBMITTED;

    @JsonCreator
    public SubmissionStatus fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(SubmissionStatus::valueOf)
                .orElse(null);
    }
}
