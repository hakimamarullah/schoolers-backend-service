package com.schoolers.exceptions;

public class NewPasswordMismatchException extends ApiException {

    public NewPasswordMismatchException() {
        super("New password and confirm password do not match");
        this.httpCode = 400;
    }
}
