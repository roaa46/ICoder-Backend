package com.icoder.core.exception;

public class EmailException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Email Exception";

    public EmailException() {
        super(DEFAULT_MESSAGE);
    }

    public EmailException(String message) {
        super(message);
    }
}
