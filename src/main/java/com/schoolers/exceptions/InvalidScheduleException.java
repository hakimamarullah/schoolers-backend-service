package com.schoolers.exceptions;

public class InvalidScheduleException extends ApiException {

    public InvalidScheduleException(String message) {
        super(message);
        this.httpCode = 400;
    }
}
