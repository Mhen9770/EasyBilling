package com.easybilling.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
@Getter
public class ValidationException extends BusinessException {
    
    private final Map<String, String> fieldErrors;
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = new HashMap<>();
    }
    
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = fieldErrors;
    }
    
    public ValidationException(Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", "Validation failed");
        this.fieldErrors = fieldErrors;
    }
}
