package com.schoolers.exceptions;

import org.springframework.aot.hint.annotation.RegisterReflection;

@RegisterReflection
public class TooManyAttempt extends ApiException {

    public TooManyAttempt(String message) {
        super(message);
        this.httpCode = 429;
    }
}
