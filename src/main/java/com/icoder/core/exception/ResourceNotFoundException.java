package com.icoder.core.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ResourceNotFoundException extends RuntimeException{
    private final Map<String, Object> details;

    public ResourceNotFoundException(String message) {
        super(message);
        this.details = null;
    }

}
