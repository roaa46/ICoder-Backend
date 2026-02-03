package com.icoder.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class TemplateException extends RuntimeException{
    private final Map<String, Object> details;

    public TemplateException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public TemplateException(String message) {
        super(message);
        this.details = null;
    }

    public TemplateException(String message, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.details = details;
    }
}
