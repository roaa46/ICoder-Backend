package com.icoder.core.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ActiveTemplateConflictException extends RuntimeException {
    private final Map<String, Object> details;
    public ActiveTemplateConflictException(String message) {
        super(message);
        details = null;
    }
}
