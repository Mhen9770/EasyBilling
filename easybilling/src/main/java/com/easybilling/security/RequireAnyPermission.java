package com.easybilling.security;

import com.easybilling.enums.Permission;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Custom annotation for checking any of multiple permissions.
 * Can be used on controller methods to check if user has at least one of the required permissions.
 * 
 * Example usage:
 * @RequireAnyPermission({Permission.PRODUCT_READ, Permission.PRODUCT_LIST})
 * public ApiResponse<List<ProductResponse>> getProducts(...) { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("@permissionChecker.hasAnyPermission(authentication, #permissions)")
public @interface RequireAnyPermission {
    Permission[] value();
}
