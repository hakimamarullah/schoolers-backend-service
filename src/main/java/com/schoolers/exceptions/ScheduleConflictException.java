package com.schoolers.exceptions;

public class ScheduleConflictException extends ApiException {

    public ScheduleConflictException(String message) {
        super(message);
        this.httpCode = 409;
    }
}
