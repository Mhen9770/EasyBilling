package com.easybilling.tenant.config;

import com.easybilling.common.dto.ApiResponse;
import com.easybilling.common.dto.ErrorDetails;
import com.easybilling.common.exception.BusinessException;
import com.easybilling.common.exception.ResourceNotFoundException;
import com.easybilling.common.exception.UnauthorizedException;
import com.easybilling.common.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for REST controllers.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        ErrorDetails error = ErrorDetails.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .build();
        return ApiResponse.error("Resource not found", error);
    }
    
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        log.error("Business exception: {}", ex.getMessage());
        ErrorDetails error = ErrorDetails.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .build();
        return ApiResponse.error(ex.getMessage(), error);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleUnauthorized(UnauthorizedException ex) {
        log.error("Unauthorized: {}", ex.getMessage());
        ErrorDetails error = ErrorDetails.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .build();
        return ApiResponse.error("Unauthorized", error);
    }
    
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(ValidationException ex) {
        log.error("Validation exception: {}", ex.getMessage());
        
        List<ErrorDetails.ValidationError> validationErrors = new ArrayList<>();
        ex.getFieldErrors().forEach((field, message) -> 
                validationErrors.add(ErrorDetails.ValidationError.builder()
                        .field(field)
                        .message(message)
                        .build())
        );
        
        ErrorDetails error = ErrorDetails.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .validationErrors(validationErrors)
                .build();
        
        return ApiResponse.error("Validation failed", error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        List<ErrorDetails.ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            
            validationErrors.add(ErrorDetails.ValidationError.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .rejectedValue(rejectedValue)
                    .build());
        });
        
        ErrorDetails error = ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .validationErrors(validationErrors)
                .build();
        
        return ApiResponse.error("Validation failed", error);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorDetails error = ErrorDetails.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .build();
        return ApiResponse.error("Internal server error", error);
    }
}
