package com.schoolers.exceptions;

public class SignatureException extends ApiException {

    public SignatureException(String message) {
        super(message);
        this.httpCode = 400;
    }
}
