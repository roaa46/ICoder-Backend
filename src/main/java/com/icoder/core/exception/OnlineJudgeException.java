package com.icoder.core.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class OnlineJudgeException extends RuntimeException {
    private final Map<String, Object> details;

    public OnlineJudgeException(String message) {
            super(message);
            this.details = null;
        }

}
