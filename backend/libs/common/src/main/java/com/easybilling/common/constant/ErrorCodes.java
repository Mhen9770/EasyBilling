package com.easybilling.common.constant;

/**
 * Centralized error codes for the platform.
 */
public final class ErrorCodes {
    
    private ErrorCodes() {
        throw new UnsupportedOperationException("Constant class");
    }
    
    // General errors
    public static final String INTERNAL_SERVER_ERROR = "ERR_INTERNAL_SERVER";
    public static final String INVALID_REQUEST = "ERR_INVALID_REQUEST";
    public static final String VALIDATION_ERROR = "ERR_VALIDATION";
    public static final String RESOURCE_NOT_FOUND = "ERR_NOT_FOUND";
    
    // Authentication & Authorization
    public static final String UNAUTHORIZED = "ERR_UNAUTHORIZED";
    public static final String FORBIDDEN = "ERR_FORBIDDEN";
    public static final String INVALID_TOKEN = "ERR_INVALID_TOKEN";
    public static final String TOKEN_EXPIRED = "ERR_TOKEN_EXPIRED";
    
    // Tenant errors
    public static final String TENANT_NOT_FOUND = "ERR_TENANT_NOT_FOUND";
    public static final String TENANT_SUSPENDED = "ERR_TENANT_SUSPENDED";
    public static final String TENANT_ALREADY_EXISTS = "ERR_TENANT_EXISTS";
    
    // Billing errors
    public static final String INSUFFICIENT_STOCK = "ERR_INSUFFICIENT_STOCK";
    public static final String INVALID_DISCOUNT = "ERR_INVALID_DISCOUNT";
    public static final String PAYMENT_FAILED = "ERR_PAYMENT_FAILED";
    
    // Product errors
    public static final String PRODUCT_NOT_FOUND = "ERR_PRODUCT_NOT_FOUND";
    public static final String DUPLICATE_BARCODE = "ERR_DUPLICATE_BARCODE";
    
    // Customer errors
    public static final String CUSTOMER_NOT_FOUND = "ERR_CUSTOMER_NOT_FOUND";
    public static final String INSUFFICIENT_LOYALTY_POINTS = "ERR_INSUFFICIENT_POINTS";
}
