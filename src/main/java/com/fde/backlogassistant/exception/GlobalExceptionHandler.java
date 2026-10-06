package com.fde.backlogassistant.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "Validation failed", "The request contains invalid data.", errors);
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleAiServiceException(AiServiceException exception) {
        LOGGER.error("AI service error: {}", exception.getMessage());
        return response(HttpStatus.BAD_GATEWAY, "AI service error", exception.getMessage(), null);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitExceededException exception) {
        return response(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded", exception.getMessage(), null);
    }

    @ExceptionHandler({DocumentTooLargeException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ApiErrorResponse> handleTooLarge(Exception exception) {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "Document too large", exception.getMessage(), null);
    }

    @ExceptionHandler(PdfProcessingException.class)
    public ResponseEntity<ApiErrorResponse> handlePdfException(PdfProcessingException exception) {
        return response(HttpStatus.BAD_REQUEST, "PDF Processing Error", exception.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        return response(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        LOGGER.error("Unexpected application error.", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred.", null);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String error, String message, Map<String, String> validation) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(Instant.now(), status.value(), error, message, validation));
    }
}
