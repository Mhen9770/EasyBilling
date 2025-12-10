package com.easybilling.exception;

import lombok.Getter;

/**
 * Base exception for business logic violations.
 * Use this for expected error conditions that should be handled gracefully.
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final Object[] args;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public BusinessException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }
}
