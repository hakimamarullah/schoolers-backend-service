package com.schoolers.exceptions;

public class TooManyAttempt extends ApiException {

    public TooManyAttempt(String message) {
        super(message);
        this.httpCode = 429;
    }
}
