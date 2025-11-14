package com.icoder.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;


@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Undetermined Exception";

    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<ErrorResponse> handlePasswordException(PasswordException ex, HttpServletRequest req) {
        String message = ex.getMessage();
        if (message==null || message.isBlank()) {
            message = INVALID_CREDENTIALS_MESSAGE;
        }
        return buildResponse(message, HttpStatus.BAD_REQUEST, req.getRequestURI());
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ErrorResponse> handleEmailException(EmailException ex, HttpServletRequest req) {
        String message = ex.getMessage();
        if (message==null || message.isBlank()) {
            message = INVALID_CREDENTIALS_MESSAGE;
        }
        return buildResponse(message, HttpStatus.CONFLICT, req.getRequestURI());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest req) {
        String message = ex.getMessage();
        if (message==null || message.isBlank()) {
            message = INVALID_CREDENTIALS_MESSAGE;
        }
        return buildResponse(message, HttpStatus.NOT_FOUND, req.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, HttpServletRequest req) {
        String message = ex.getMessage();
        if (message==null || message.isBlank()) {
            message = INVALID_CREDENTIALS_MESSAGE;
        }
        return buildResponse(message, HttpStatus.BAD_REQUEST, req.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        String message = ex.getMessage();
        if (message==null || message.isBlank()) {
            message = INVALID_CREDENTIALS_MESSAGE;
        }
        return buildResponse(message, HttpStatus.UNAUTHORIZED, req.getRequestURI());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest req) {
        String message = ex.getMessage();
        if (message==null || message.isBlank()) {
            message = INVALID_CREDENTIALS_MESSAGE;
        }
        return buildResponse(message, HttpStatus.INTERNAL_SERVER_ERROR, req.getRequestURI());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest req) {
        String message = ex.getMessage();
        if (message==null || message.isBlank()) {
            message = INVALID_CREDENTIALS_MESSAGE;
        }
        return buildResponse(message, HttpStatus.BAD_REQUEST, req.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status, String path) {
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        return ResponseEntity.status(status).body(body);
    }

    record ErrorResponse(String timestamp, int status, String error, String message, String path) {}
}