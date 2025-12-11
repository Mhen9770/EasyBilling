package com.easybilling.security;

import com.easybilling.enums.Permission;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Custom annotation for permission-based access control.
 * Can be used on controller methods to check if user has required permission.
 * 
 * Example usage:
 * @RequirePermission(Permission.PRODUCT_CREATE)
 * public ApiResponse<ProductResponse> createProduct(...) { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasPermission(null, #permission)")
public @interface RequirePermission {
    Permission value();
}
