package com.easybilling.auth.controller;

import com.easybilling.auth.dto.LoginRequest;
import com.easybilling.auth.dto.LoginResponse;
import com.easybilling.auth.dto.RefreshTokenRequest;
import com.easybilling.auth.dto.RegisterRequest;
import com.easybilling.auth.service.AuthService;
import com.easybilling.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and get JWT tokens")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("Login successful", response);
    }
    
    @PostMapping("/onboard")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tenant onboarding", description = "Complete tenant self-service onboarding - creates tenant and admin user")
    public ApiResponse<LoginResponse> onboard(@Valid @RequestBody RegisterRequest request) {
        // This endpoint creates a new tenant and the first admin user
        // It's public for self-service tenant registration
        LoginResponse response = authService.onboard(request);
        return ApiResponse.success("Tenant onboarding successful", response);
    }
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "User registration (Admin Only)", description = "Register a new user within an existing tenant - requires admin authentication")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        // This endpoint is for adding users to an existing tenant
        // Requires admin authentication (enforced by SecurityFilterChain)
        LoginResponse response = authService.register(request);
        return ApiResponse.success("User registration successful", response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ApiResponse.success("Token refreshed", response);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user (client should discard tokens)")
    public ApiResponse<Void> logout() {
        // In stateless JWT, logout is handled client-side
        // Optionally, implement token blacklisting here
        return ApiResponse.success("Logout successful", null);
    }
}
