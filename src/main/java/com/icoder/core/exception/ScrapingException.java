package com.icoder.core.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ScrapingException extends RuntimeException {
    private final Map<String, Object> details;

    public ScrapingException(String message) {
        super(message);
        this.details = null;
    }

    public ScrapingException(String message, Throwable cause) {
        super(message, cause);
        this.details = null;
    }

}

