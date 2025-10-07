package com.schoolers.exceptions;

import org.springframework.aot.hint.annotation.RegisterReflection;

@RegisterReflection
public class DataNotFoundException extends ApiException {

    public DataNotFoundException(String message) {
        super(message);
        this.httpCode = 404;
    }
}
