package com.easybilling.resolver;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 */
@Slf4j
@Component
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;
    
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-validity-ms:3600000}") long accessTokenValidityMs,
            @Value("${app.jwt.refresh-token-validity-ms:604800000}") long refreshTokenValidityMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }
    
    /**
     * Generate access token with user details.
     */
    public String generateAccessToken(String userId, String tenantId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("tenantId", tenantId);
        claims.put("roles", roles);
        claims.put("type", "access");
        
        return generateToken(claims, userId, accessTokenValidityMs);
    }
    
    /**
     * Generate refresh token.
     */
    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        
        return generateToken(claims, userId, refreshTokenValidityMs);
    }
    
    /**
     * Generate JWT token with claims.
     */
    private String generateToken(Map<String, Object> claims, String subject, long validityMs) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityMs);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }
    
    /**
     * Validate JWT token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Get user ID from token.
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }
    
    /**
     * Get tenant ID from token.
     */
    public String getTenantIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("tenantId", String.class);
    }
    
    /**
     * Get roles from token.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("roles", List.class);
    }
    
    /**
     * Get token type (access or refresh).
     */
    public String getTokenType(String token) {
        Claims claims = getClaims(token);
        return claims.get("type", String.class);
    }
    
    /**
     * Extract all claims from token.
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Check if token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
