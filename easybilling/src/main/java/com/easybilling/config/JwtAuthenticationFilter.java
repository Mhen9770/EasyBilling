package com.easybilling.config;

import com.easybilling.context.TenantContext;
import com.easybilling.context.UserContext;
import com.easybilling.resolver.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter.
 * Extracts JWT token from Authorization header, validates it, and sets security context.
 * Also extracts tenant ID and user ID from token and sets them in context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract token from Authorization header
            String token = extractToken(request);
            
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // Extract user ID and tenant ID from token
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                Integer tenantId = jwtTokenProvider.getTenantIdFromToken(token);
                List<String> roles = jwtTokenProvider.getRolesFromToken(token);
                
                // Set in thread-local context
                UserContext.setUserId(userId);
                TenantContext.setTenantId(tenantId);
                
                log.debug("Authenticated user: {} for tenant: {} with roles: {}", userId, tenantId, roles);
                
                // Set Spring Security context
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> {
                                // Ensure role has ROLE_ prefix for Spring Security
                                String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                                return new SimpleGrantedAuthority(roleName);
                            })
                            .collect(Collectors.toList());
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Failed to set authentication context", e);
            // Don't block the request - let it continue (might be a public endpoint)
        } finally {
            try {
                filterChain.doFilter(request, response);
            } finally {
                // Clean up thread-local context after request
                TenantContext.clear();
                UserContext.clear();
            }
        }
    }
    
    /**
     * Extract JWT token from Authorization header.
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip filter for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            return true;
        }
        
        // Skip filter for public endpoints
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/onboard") ||
               path.startsWith("/api/v1/auth/refresh") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}

