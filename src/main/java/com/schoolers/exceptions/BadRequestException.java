package com.schoolers.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(message);
        this.httpCode = HttpStatus.BAD_REQUEST.value();
    }
}
