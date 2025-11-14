package com.icoder.core.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
