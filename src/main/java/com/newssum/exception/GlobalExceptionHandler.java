package com.newssum.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.newssum.dto.common.ApiResponse;

/**
 * Converts exceptions into standardized API responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(final BusinessException exception) {
        final ErrorCode errorCode = exception.getErrorCode();
        log.warn("Business exception: {}", errorCode.name(), exception);
        return ResponseEntity.status(errorCode.getStatus())
            .body(ApiResponse.failure(errorCode.name(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(final MethodArgumentNotValidException exception) {
        final String message = exception.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .findFirst()
            .orElse(ErrorCode.INVALID_REQUEST.getMessage());
        log.warn("Validation failed: {}", message);
        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getStatus())
            .body(ApiResponse.failure(ErrorCode.INVALID_REQUEST.name(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(final Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
            .body(ApiResponse.failure(ErrorCode.INTERNAL_ERROR.name(), ErrorCode.INTERNAL_ERROR.getMessage()));
    }
}
