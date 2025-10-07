package com.schoolers.exceptions;

import org.springframework.aot.hint.annotation.RegisterReflection;

@RegisterReflection
public class DuplicateDataException extends ApiException {

    public DuplicateDataException(String message) {
        super(message);
        this.httpCode = 409;
    }
}
