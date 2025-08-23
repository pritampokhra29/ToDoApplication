package com.example.demo.filter;

import com.example.demo.util.CustomLogger;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter implements Filter {

    private static final CustomLogger logger = CustomLogger.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Generate unique request ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();
        
        // Extract request details
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String contentType = httpRequest.getContentType();
        
        // Set MDC for this request
        MDC.put("requestId", requestId);
        MDC.put("clientIp", clientIp);
        MDC.put("uri", uri);
        MDC.put("method", method);
        
        try {
            // Log request start
            logger.auditInfo("REQUEST_START - RequestID: {}, Method: {}, URI: {}, QueryString: {}, ClientIP: {}, UserAgent: {}, ContentType: {}", 
                    requestId, method, uri, queryString, clientIp, userAgent, contentType);
            
            // Continue with the request
            chain.doFilter(request, response);
            
            // Get authentication after processing (in case it was set during the request)
            String username = getCurrentUsername();
            MDC.put("username", username);
            
            // Log request completion
            long executionTime = System.currentTimeMillis() - startTime;
            int statusCode = httpResponse.getStatus();
            
            logger.auditInfo("REQUEST_END - RequestID: {}, Method: {}, URI: {}, Username: {}, StatusCode: {}, ExecutionTime: {}ms, ClientIP: {}", 
                    requestId, method, uri, username, statusCode, executionTime, clientIp);
            
            // Log performance warning for slow requests
            if (executionTime > 5000) {
                logger.logPerformance("HTTP_REQUEST", executionTime, "SLOW");
            } else if (executionTime > 2000) {
                logger.logPerformance("HTTP_REQUEST", executionTime, "MODERATE");
            }
            
            // Log security events for failed authentications
            if (statusCode == 401) {
                logger.logSecurityEvent("AUTHENTICATION_FAILED", username, 
                        "Failed authentication attempt from " + clientIp + " for URI: " + uri, "MEDIUM");
            } else if (statusCode == 403) {
                logger.logSecurityEvent("AUTHORIZATION_FAILED", username, 
                        "Failed authorization attempt from " + clientIp + " for URI: " + uri, "MEDIUM");
            } else if (statusCode >= 400 && statusCode < 500) {
                logger.warn("CLIENT_ERROR - RequestID: {}, StatusCode: {}, URI: {}, Username: {}, ClientIP: {}", 
                        requestId, statusCode, uri, username, clientIp);
            } else if (statusCode >= 500) {
                logger.error("SERVER_ERROR - RequestID: {}, StatusCode: {}, URI: {}, Username: {}, ClientIP: {}", 
                        requestId, statusCode, uri, username, clientIp);
            }
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.logException("REQUEST_PROCESSING", e, 
                    String.format("RequestID: %s, Method: %s, URI: %s, ExecutionTime: %dms", 
                            requestId, method, uri, executionTime));
            throw e;
        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Security context not available
        }
        return "anonymous";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String xForwarded = request.getHeader("X-Forwarded");
        if (xForwarded != null && !xForwarded.isEmpty() && !"unknown".equalsIgnoreCase(xForwarded)) {
            return xForwarded;
        }
        
        String forwarded = request.getHeader("Forwarded");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            return forwarded;
        }
        
        return request.getRemoteAddr();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RequestLoggingFilter initialized");
    }

    @Override
    public void destroy() {
        logger.info("RequestLoggingFilter destroyed");
    }
}
