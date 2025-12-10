package com.easybilling.common.exception;

/**
 * Exception thrown when a user is not authorized to perform an action.
 */
public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
    
    public UnauthorizedException() {
        super("UNAUTHORIZED", "You are not authorized to perform this action");
    }
}
