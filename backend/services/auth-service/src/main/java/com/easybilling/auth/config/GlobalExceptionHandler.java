package com.easybilling.auth.config;

import com.easybilling.common.dto.ApiResponse;
import com.easybilling.common.dto.ErrorDetails;
import com.easybilling.common.exception.BusinessException;
import com.easybilling.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for Auth Service.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
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
    
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleBadCredentials(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        ErrorDetails error = ErrorDetails.builder()
                .code("BAD_CREDENTIALS")
                .message("Invalid username or password")
                .build();
        return ApiResponse.error("Authentication failed", error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
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
