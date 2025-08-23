package com.example.demo.service;

import com.example.demo.dto.JwtAuthenticationResponse;
import com.example.demo.dto.UserDTO;
import com.example.demo.util.CustomLogger;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    private static final CustomLogger logger = CustomLogger.getLogger(AuthenticationService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    /**
     * Authenticate user and generate JWT tokens
     */
    public JwtAuthenticationResponse authenticateUser(UserDTO loginRequest) {
        try {
            // Check if JWT is enabled
            if (!jwtUtil.isJwtEnabled()) {
                logger.logSecurityEvent("JWT_DISABLED_LOGIN_ATTEMPT", loginRequest.getUsername(),
                        "JWT authentication attempted but JWT is disabled", "MEDIUM");
                throw new IllegalStateException("JWT authentication is disabled. Use Basic Authentication instead.");
            }
            
            // Log authentication attempt
            logger.logSecurityEvent("AUTHENTICATION_ATTEMPT", loginRequest.getUsername(),
                    "User attempting to login with JWT", "LOW");

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Generate tokens
            String accessToken = jwtUtil.generateToken(authentication);
            String refreshToken = jwtUtil.generateRefreshToken(authentication);

            // Extract user details
            String username = authentication.getName();
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            // Calculate expiration times
            Date accessTokenExpiration = jwtUtil.getExpirationDateFromToken(accessToken);
            long expiresInSeconds = (accessTokenExpiration.getTime() - new Date().getTime()) / 1000;

            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = LocalDateTime.ofInstant(
                    accessTokenExpiration.toInstant(), 
                    ZoneId.systemDefault()
            );

            // Create response
            JwtAuthenticationResponse response = new JwtAuthenticationResponse(
                    accessToken, refreshToken, username, roles, 
                    expiresInSeconds, issuedAt, expiresAt
            );

            // Log successful authentication
            logger.logSecurityEvent("AUTHENTICATION_SUCCESS", username,
                    "User successfully authenticated", "LOW");
            logger.logUserActivity(username, "LOGIN", "/auth/login", 
                    "User logged in successfully with JWT tokens");

            return response;

        } catch (BadCredentialsException e) {
            logger.logSecurityEvent("AUTHENTICATION_FAILED", loginRequest.getUsername(),
                    "Invalid credentials provided", "MEDIUM");
            throw new BadCredentialsException("Invalid username or password");
        } catch (AuthenticationException e) {
            logger.logSecurityEvent("AUTHENTICATION_ERROR", loginRequest.getUsername(),
                    "Authentication error: " + e.getMessage(), "MEDIUM");
            throw e;
        } catch (Exception e) {
            logger.logException("AUTHENTICATION_EXCEPTION", e, 
                    "Unexpected error during authentication for user: " + loginRequest.getUsername());
            throw new RuntimeException("Authentication failed due to system error");
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        try {
            logger.logSecurityEvent("TOKEN_REFRESH_ATTEMPT", "unknown",
                    "Attempting to refresh access token", "LOW");

            // Check if refresh token is blacklisted
            if (tokenBlacklistService.isBlacklisted(refreshToken)) {
                logger.logSecurityEvent("TOKEN_REFRESH_BLACKLISTED", "unknown",
                        "Attempting to use blacklisted refresh token", "HIGH");
                throw new BadCredentialsException("Refresh token has been invalidated");
            }

            // Validate refresh token
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                logger.logSecurityEvent("TOKEN_REFRESH_FAILED", "unknown",
                        "Invalid refresh token provided", "MEDIUM");
                throw new BadCredentialsException("Invalid refresh token");
            }

            // Extract username from refresh token
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            
            // Verify this is actually a refresh token (redundant check but kept for safety)
            String tokenType = jwtUtil.getAllClaimsFromToken(refreshToken).get("tokenType", String.class);
            if (!"REFRESH".equals(tokenType)) {
                logger.logSecurityEvent("TOKEN_REFRESH_INVALID_TYPE", username,
                        "Non-refresh token used for refresh", "HIGH");
                throw new BadCredentialsException("Invalid token type for refresh");
            }

            // Generate new access token
            String newAccessToken = jwtUtil.generateTokenFromRefreshToken(refreshToken);

            // Extract user roles from refresh token (if available) or load from database
            Set<String> roles = jwtUtil.getRolesFromToken(refreshToken);

            // Calculate expiration times
            Date accessTokenExpiration = jwtUtil.getExpirationDateFromToken(newAccessToken);
            long expiresInSeconds = (accessTokenExpiration.getTime() - new Date().getTime()) / 1000;

            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = LocalDateTime.ofInstant(
                    accessTokenExpiration.toInstant(), 
                    ZoneId.systemDefault()
            );

            // Create response (keeping the same refresh token)
            JwtAuthenticationResponse response = new JwtAuthenticationResponse(
                    newAccessToken, refreshToken, username, roles, 
                    expiresInSeconds, issuedAt, expiresAt
            );

            logger.logSecurityEvent("TOKEN_REFRESH_SUCCESS", username,
                    "Access token refreshed successfully", "LOW");
            logger.logUserActivity(username, "TOKEN_REFRESH", "/auth/refresh", 
                    "User refreshed access token");

            return response;

        } catch (Exception e) {
            logger.logException("TOKEN_REFRESH_EXCEPTION", e, 
                    "Error refreshing token");
            throw new BadCredentialsException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * Validate token and return user details
     */
    public boolean validateToken(String token) {
        try {
            boolean isValid = jwtUtil.validateToken(token);
            
            if (isValid) {
                String username = jwtUtil.getUsernameFromToken(token);
                logger.debug("Token validation successful for user: {}", username);
            } else {
                logger.logSecurityEvent("TOKEN_VALIDATION_FAILED", "unknown",
                        "Token validation failed", "MEDIUM");
            }
            
            return isValid;
        } catch (Exception e) {
            logger.logException("TOKEN_VALIDATION_EXCEPTION", e, 
                    "Error validating token");
            return false;
        }
    }

    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        try {
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            logger.logException("USERNAME_EXTRACTION_EXCEPTION", e, 
                    "Error extracting username from token");
            return null;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            logger.logException("TOKEN_EXPIRY_CHECK_EXCEPTION", e, 
                    "Error checking token expiry");
            return true; // Assume expired on error
        }
    }

    /**
     * Get token remaining validity in seconds
     */
    public long getTokenRemainingValidity(String token) {
        try {
            return jwtUtil.getTokenValidityDuration(token) / 1000; // Convert to seconds
        } catch (Exception e) {
            logger.logException("TOKEN_VALIDITY_CHECK_EXCEPTION", e, 
                    "Error checking token validity duration");
            return 0;
        }
    }
}
