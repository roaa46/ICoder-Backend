package com.icoder.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class OnlineJudgeException extends RuntimeException {
    private final Map<String, Object> details;

    public OnlineJudgeException(String message, Map<String, Object> details) {
            super(message);
            this.details = details;
        }

    public OnlineJudgeException(String message) {
            super(message);
            this.details = null;
        }

    public OnlineJudgeException(String message, HttpStatus status, Map<String, Object> details) {
            super(message);
            this.details = details;
        }
}
