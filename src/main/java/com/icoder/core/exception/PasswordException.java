package com.icoder.core.exception;

public class PasswordException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Password Exception";

    public PasswordException() {
        super(DEFAULT_MESSAGE);
    }

    public PasswordException(String message) {
        super(message);
    }
}
