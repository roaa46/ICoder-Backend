package com.icoder.core.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class SubmissionException extends RuntimeException {
    private final Map<String, Object> details;

    public SubmissionException(String message) {
        super(message);
        this.details = null;
    }
}
