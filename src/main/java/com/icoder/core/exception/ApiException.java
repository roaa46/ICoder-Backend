package com.icoder.core.exception;

import lombok.*;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {
    private final Map<String, Object> details;

    public ApiException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public ApiException(String message) {
        super(message);
        this.details = null;
    }

}
