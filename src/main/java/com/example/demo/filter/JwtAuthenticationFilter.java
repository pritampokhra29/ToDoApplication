package com.example.demo.filter;

import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.TokenBlacklistService;
import com.example.demo.util.CustomLogger;
import com.example.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final CustomLogger logger = CustomLogger.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            System.out.println("DEBUG: JWT Filter processing request: " + request.getRequestURI());
            
            // Skip JWT processing if JWT is disabled
            if (!jwtUtil.isJwtEnabled()) {
                System.out.println("DEBUG: JWT is disabled, skipping JWT authentication filter");
                logger.debug("JWT is disabled, skipping JWT authentication filter");
                filterChain.doFilter(request, response);
                return;
            }
            
            System.out.println("DEBUG: JWT is enabled, processing authentication");
            
            String jwt = getJwtFromRequest(request);
            System.out.println("DEBUG: Extracted JWT token: " + (jwt != null ? "present (length: " + jwt.length() + ")" : "null"));
            
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                System.out.println("DEBUG: JWT token is valid");
                
                // Check if token is blacklisted
                if (tokenBlacklistService.isBlacklisted(jwt)) {
                    System.out.println("DEBUG: JWT token is blacklisted");
                    logger.logSecurityEvent("JWT_BLACKLISTED_TOKEN", "unknown", 
                            "Authentication attempt with blacklisted token", "HIGH");
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Token has been invalidated\"}");
                    return;
                }
                
                String username = jwtUtil.getUsernameFromToken(jwt);
                System.out.println("DEBUG: Username from JWT: " + username);
                
                logger.debug("JWT token found for user: {}", username);
                
                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Debug: Log the authorities being set
                logger.debug("Setting authorities for user {}: {}", username, userDetails.getAuthorities());
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("DEBUG: Authentication set in SecurityContext for user: " + username + " with authorities: " + userDetails.getAuthorities());
                
                logger.logSecurityEvent("JWT_AUTHENTICATION_SUCCESS", username, 
                        "User authenticated via JWT token", "LOW");
                
                // Log token validity information
                long validityDuration = jwtUtil.getTokenValidityDuration(jwt);
                if (validityDuration < 3600000) { // Less than 1 hour
                    logger.logSecurityEvent("JWT_TOKEN_EXPIRING_SOON", username, 
                            "Token expires in " + (validityDuration / 60000) + " minutes", "LOW");
                }
                
            } else if (jwt != null) {
                // Invalid or expired token
                System.out.println("DEBUG: JWT token is invalid or expired");
                String clientIp = getClientIpAddress(request);
                logger.logSecurityEvent("JWT_AUTHENTICATION_FAILED", "unknown", 
                        "Invalid or expired JWT token from IP: " + clientIp, "MEDIUM");
                
                // Clear any existing authentication
                SecurityContextHolder.clearContext();
                
                // Set unauthorized status for invalid tokens
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                return;
            } else {
                System.out.println("DEBUG: No JWT token found in request");
            }
            
        } catch (Exception e) {
            logger.logException("JWT_FILTER_ERROR", e, 
                    "Error processing JWT authentication for request: " + request.getRequestURI());
            
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        
        // Also check for token in query parameter (for WebSocket connections, etc.)
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            logger.logSecurityEvent("JWT_FROM_QUERY_PARAM", "unknown", 
                    "JWT token received via query parameter", "LOW");
            return tokenParam;
        }
        
        return null;
    }

    /**
     * Get client IP address handling proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Skip JWT authentication for certain endpoints
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip JWT filter for authentication endpoints
        return path.equals("/auth/login") || 
               path.equals("/auth/refresh") ||
               path.equals("/auth/config") ||
               path.equals("/debug/auth") ||  // Temporarily allow debug endpoint
               path.startsWith("/h2-console") ||
               path.startsWith("/actuator") ||
               path.equals("/favicon.ico");
    }
}
