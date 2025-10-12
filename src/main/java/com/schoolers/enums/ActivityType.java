package com.schoolers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;

public enum ActivityType {
    CREATE_TEACHER,
    UPDATE_TEACHER,
    DELETE_TEACHER,
    CREATE_CLASSROOM,
    UPDATE_CLASSROOM,
    DELETE_CLASSROOM,
    CREATE_SUBJECT,
    UPDATE_SUBJECT,
    DELETE_SUBJECT,
    ASSIGN_TEACHER_TO_CLASS,
    REMOVE_TEACHER_FROM_CLASS,
    CREATE_STUDENT,
    UPDATE_STUDENT,
    DELETE_STUDENT,
    CREATE_SCHEDULE,
    UPDATE_SCHEDULE,
    DELETE_SCHEDULE,
    CREATE_ASSIGNMENT,
    UPDATE_ASSIGNMENT,
    DELETE_ASSIGNMENT,
    RECORD_ATTENDANCE,
    UPDATE_ATTENDANCE;

    @JsonCreator
    public ActivityType fromString(String name) {
        return Optional.ofNullable(name)
                .map(String::toUpperCase)
                .map(ActivityType::valueOf)
                .orElse(null);
    }
}
