package com.icoder.core.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ProblemNotFoundException extends RuntimeException{
    private final Map<String, Object> details;

    public ProblemNotFoundException(String message) {
        super(message);
        this.details = null;
    }

}
