package com.example.demo.util;

import com.example.demo.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final CustomLogger logger = CustomLogger.getLogger(JwtUtil.class);

    @Autowired
    private JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        if (!jwtProperties.isEnabled()) {
            throw new IllegalStateException("JWT is disabled. Cannot generate signing key.");
        }
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Check if JWT is enabled
     */
    public boolean isJwtEnabled() {
        return jwtProperties.isEnabled();
    }

    /**
     * Generate JWT token for authenticated user
     */
    public String generateToken(Authentication authentication) {
        if (!jwtProperties.isEnabled()) {
            throw new IllegalStateException("JWT is disabled. Cannot generate token.");
        }
        
        String username = authentication.getName();
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("tokenType", "ACCESS");

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .claims(claims)
                .signWith(getSigningKey())
                .compact();

        logger.logSecurityEvent("JWT_TOKEN_GENERATED", username, 
                "Access token generated with expiration: " + expiryDate, "LOW");

        return token;
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(Authentication authentication) {
        if (!jwtProperties.isEnabled()) {
            throw new IllegalStateException("JWT is disabled. Cannot generate refresh token.");
        }
        
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefresh().getExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH");

        String refreshToken = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .claims(claims)
                .signWith(getSigningKey())
                .compact();

        logger.logSecurityEvent("JWT_REFRESH_TOKEN_GENERATED", username, 
                "Refresh token generated with expiration: " + expiryDate, "LOW");

        return refreshToken;
    }

    /**
     * Generate access token from refresh token
     */
    public String generateTokenFromRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            String username = claims.getSubject();
            String tokenType = claims.get("tokenType", String.class);

            if (!"REFRESH".equals(tokenType)) {
                logger.logSecurityEvent("JWT_INVALID_TOKEN_TYPE", username, 
                        "Attempted to refresh with non-refresh token", "MEDIUM");
                throw new JwtException("Invalid token type for refresh");
            }

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put("tokenType", "ACCESS");

            String newToken = Jwts.builder()
                    .subject(username)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .claims(newClaims)
                    .signWith(getSigningKey())
                    .compact();

            logger.logSecurityEvent("JWT_TOKEN_REFRESHED", username, 
                    "Access token refreshed successfully", "LOW");

            return newToken;

        } catch (JwtException e) {
            logger.logSecurityEvent("JWT_REFRESH_FAILED", "unknown", 
                    "Failed to refresh token: " + e.getMessage(), "MEDIUM");
            throw e;
        }
    }

    /**
     * Extract username from JWT token
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (JwtException e) {
            logger.logSecurityEvent("JWT_USERNAME_EXTRACTION_FAILED", "unknown", 
                    "Failed to extract username from token: " + e.getMessage(), "MEDIUM");
            throw e;
        }
    }

    /**
     * Extract roles from JWT token
     */
    @SuppressWarnings("unchecked")
    public Set<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return (Set<String>) claims.get("roles");
        } catch (JwtException e) {
            logger.logSecurityEvent("JWT_ROLES_EXTRACTION_FAILED", "unknown", 
                    "Failed to extract roles from token: " + e.getMessage(), "MEDIUM");
            throw e;
        }
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();
        } catch (JwtException e) {
            logger.logSecurityEvent("JWT_EXPIRATION_EXTRACTION_FAILED", "unknown", 
                    "Failed to extract expiration from token: " + e.getMessage(), "MEDIUM");
            throw e;
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("tokenType", String.class);
            if (!"ACCESS".equals(tokenType)) {
                logger.logSecurityEvent("JWT_INVALID_TOKEN_TYPE", claims.getSubject(), 
                        "Invalid token type for access: " + tokenType, "MEDIUM");
                return false;
            }

            return true;
        } catch (SecurityException e) {
            logger.logSecurityEvent("JWT_SECURITY_EXCEPTION", "unknown", 
                    "JWT signature does not match locally computed signature", "HIGH");
        } catch (MalformedJwtException e) {
            logger.logSecurityEvent("JWT_MALFORMED", "unknown", 
                    "Invalid JWT token format", "MEDIUM");
        } catch (ExpiredJwtException e) {
            logger.logSecurityEvent("JWT_EXPIRED", e.getClaims().getSubject(), 
                    "JWT token has expired", "LOW");
        } catch (UnsupportedJwtException e) {
            logger.logSecurityEvent("JWT_UNSUPPORTED", "unknown", 
                    "JWT token is unsupported", "MEDIUM");
        } catch (IllegalArgumentException e) {
            logger.logSecurityEvent("JWT_ILLEGAL_ARGUMENT", "unknown", 
                    "JWT claims string is empty", "MEDIUM");
        } catch (JwtException e) {
            logger.logSecurityEvent("JWT_GENERAL_ERROR", "unknown", 
                    "JWT validation error: " + e.getMessage(), "MEDIUM");
        }
        return false;
    }

    /**
     * Validate refresh token specifically
     * @param token JWT refresh token
     * @return true if valid refresh token, false otherwise
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("tokenType", String.class);
            if (!"REFRESH".equals(tokenType)) {
                logger.logSecurityEvent("JWT_INVALID_REFRESH_TOKEN_TYPE", claims.getSubject(), 
                        "Invalid token type for refresh: " + tokenType, "MEDIUM");
                return false;
            }

            return true;
        } catch (SecurityException e) {
            logger.logSecurityEvent("JWT_REFRESH_SECURITY_EXCEPTION", "unknown", 
                    "JWT refresh token signature does not match", "HIGH");
        } catch (MalformedJwtException e) {
            logger.logSecurityEvent("JWT_REFRESH_MALFORMED", "unknown", 
                    "Invalid JWT refresh token format", "MEDIUM");
        } catch (ExpiredJwtException e) {
            logger.logSecurityEvent("JWT_REFRESH_EXPIRED", e.getClaims().getSubject(), 
                    "JWT refresh token has expired", "LOW");
        } catch (UnsupportedJwtException e) {
            logger.logSecurityEvent("JWT_REFRESH_UNSUPPORTED", "unknown", 
                    "JWT refresh token is unsupported", "MEDIUM");
        } catch (IllegalArgumentException e) {
            logger.logSecurityEvent("JWT_REFRESH_ILLEGAL_ARGUMENT", "unknown", 
                    "JWT refresh token claims string is empty", "MEDIUM");
        } catch (JwtException e) {
            logger.logSecurityEvent("JWT_REFRESH_GENERAL_ERROR", "unknown", 
                    "JWT refresh token validation error: " + e.getMessage(), "MEDIUM");
        }
        return false;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true; // Consider invalid tokens as expired
        }
    }

    /**
     * Get remaining validity time in milliseconds
     */
    public long getTokenValidityDuration(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - new Date().getTime();
        } catch (JwtException e) {
            return 0;
        }
    }

    /**
     * Extract all claims from token
     */
    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            logger.logSecurityEvent("JWT_CLAIMS_EXTRACTION_FAILED", "unknown", 
                    "Failed to extract claims from token: " + e.getMessage(), "MEDIUM");
            throw e;
        }
    }

    /**
     * Create token with custom expiration
     */
    public String generateTokenWithCustomExpiration(Authentication authentication, long expirationMs) {
        String username = authentication.getName();
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("tokenType", "ACCESS");

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .claims(claims)
                .signWith(getSigningKey())
                .compact();

        logger.logSecurityEvent("JWT_CUSTOM_TOKEN_GENERATED", username, 
                "Custom expiration token generated: " + expirationMs + "ms", "LOW");

        return token;
    }
}
