package com.icoder.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ProblemNotFoundException extends RuntimeException{
    private final Map<String, Object> details;

    public ProblemNotFoundException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public ProblemNotFoundException(String message) {
        super(message);
        this.details = null;
    }

    public ProblemNotFoundException(String message, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.details = details;
    }
}
