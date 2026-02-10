package com.icoder.core.exception;

import com.icoder.core.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // handling custom ApiException
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        log.error("API Exception");
        HttpStatus status = resolveStatus(ex);
        return buildErrorResponse(status, ex.getMessage(), request.getRequestURI(), ex.getDetails());
    }
    @ExceptionHandler(ActiveTemplateConflictException.class)
    public ResponseEntity<ErrorResponse> handleActiveTemplateConflictException(ActiveTemplateConflictException ex, HttpServletRequest request) {
        log.error("ActiveTemplateConflict Exception ");
        HttpStatus status = resolveStatus(ex);
        return buildErrorResponse(status, ex.getMessage(), request.getRequestURI(), ex.getDetails());
    }
    @ExceptionHandler(TemplateException.class)
    public ResponseEntity<ErrorResponse> handleTemplateException(TemplateException ex, HttpServletRequest request) {
        log.error("Template Exception");
        HttpStatus status = resolveStatus(ex);
        return buildErrorResponse(status, ex.getMessage(), request.getRequestURI(), ex.getDetails());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("ResourceNotFound Exception");
        HttpStatus status = resolveStatus(ex);
        return buildErrorResponse(status, ex.getMessage(), request.getRequestURI(), ex.getDetails());
    }

    @ExceptionHandler(ScrapingException.class)
    public ResponseEntity<ErrorResponse> handleScrapingException(ScrapingException ex, HttpServletRequest request) {
        log.error("Scraping Exception");
        HttpStatus status = resolveStatus(ex);
        return buildErrorResponse(status, ex.getMessage(), request.getRequestURI(), ex.getDetails());
    }

    @ExceptionHandler(OnlineJudgeException.class)
    public ResponseEntity<ErrorResponse> handleOnlineJudgeException(OnlineJudgeException ex, HttpServletRequest request) {
        log.error("OnlineJudge Exception");
        HttpStatus status = resolveStatus(ex);
        return buildErrorResponse(status, ex.getMessage(), request.getRequestURI(), ex.getDetails());
    }

    // handling any unexpected exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected Exception");
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, Object> details
    ) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status.value())
                .error(status.name())
                .message(message)
                .path(path)
                .details(details)
                .build();

        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus resolveStatus(Exception ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}