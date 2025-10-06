package com.schoolers.exceptions;

public class DuplicateDataException extends ApiException {

    public DuplicateDataException(String message) {
        super(message);
        this.httpCode = 409;
    }
}
