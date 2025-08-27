package com.example.demo.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger methodLogger = LoggerFactory.getLogger("METHOD_EXECUTION");

    @Pointcut("execution(* com.example.demo.controller.*.*(..))")
    public void controllerMethods() {}

    @Pointcut("execution(* com.example.demo.service.*.*(..))")
    public void serviceMethods() {}

    @Around("controllerMethods() || serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String username = getCurrentUsername();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        // Set MDC for consistent logging across the request
        MDC.put("requestId", requestId);
        MDC.put("username", username);
        MDC.put("method", methodName);
        
        // Get HTTP request details if available
        String uri = "";
        String clientIp = "";
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                uri = request.getRequestURI();
                clientIp = getClientIpAddress(request);
                MDC.put("uri", uri);
                MDC.put("clientIp", clientIp);
            }
        } catch (Exception e) {
            // Request context not available (e.g., for service methods called outside web context)
        }

        // Log method start
        auditLogger.info("METHOD_START - Class: {}, Method: {}, Username: {}, URI: {}, ClientIP: {}, RequestID: {}, Args: {}", 
                className, methodName, username, uri, clientIp, requestId, Arrays.toString(joinPoint.getArgs()));
        
        methodLogger.info("Starting execution - Class: {}, Method: {}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log successful completion
            auditLogger.info("METHOD_SUCCESS - Class: {}, Method: {}, Username: {}, URI: {}, ClientIP: {}, RequestID: {}, ExecutionTime: {}ms", 
                    className, methodName, username, uri, clientIp, requestId, executionTime);
            
            methodLogger.info("Completed execution - Class: {}, Method: {}, ExecutionTime: {}ms", 
                    className, methodName, executionTime);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log error
            auditLogger.error("METHOD_ERROR - Class: {}, Method: {}, Username: {}, URI: {}, ClientIP: {}, RequestID: {}, ExecutionTime: {}ms, Error: {}", 
                    className, methodName, username, uri, clientIp, requestId, executionTime, e.getMessage());
            
            methodLogger.error("Error in execution - Class: {}, Method: {}, ExecutionTime: {}ms, Error: {}", 
                    className, methodName, executionTime, e.getMessage());
            
            throw e;
        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    @Before("controllerMethods()")
    public void logControllerEntry(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String username = getCurrentUsername();
        
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String uri = request.getRequestURI();
                String clientIp = getClientIpAddress(request);
                String httpMethod = request.getMethod();
                
                auditLogger.info("CONTROLLER_ENTRY - Class: {}, Method: {}, Username: {}, URI: {}, HTTPMethod: {}, ClientIP: {}", 
                        className, methodName, username, uri, httpMethod, clientIp);
            }
        } catch (Exception e) {
            auditLogger.warn("Could not log controller entry details for {}.{}: {}", className, methodName, e.getMessage());
        }
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logControllerReturn(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String username = getCurrentUsername();
        
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String uri = request.getRequestURI();
                String clientIp = getClientIpAddress(request);
                
                auditLogger.info("CONTROLLER_RETURN - Class: {}, Method: {}, Username: {}, URI: {}, ClientIP: {}, ReturnType: {}", 
                        className, methodName, username, uri, clientIp, 
                        result != null ? result.getClass().getSimpleName() : "null");
            }
        } catch (Exception e) {
            auditLogger.warn("Could not log controller return details for {}.{}: {}", className, methodName, e.getMessage());
        }
    }

    @AfterThrowing(pointcut = "controllerMethods() || serviceMethods()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().toShortString();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String username = getCurrentUsername();
        
        try {
            HttpServletRequest request = getCurrentRequest();
            String uri = "";
            String clientIp = "";
            if (request != null) {
                uri = request.getRequestURI();
                clientIp = getClientIpAddress(request);
            }
            
            auditLogger.error("EXCEPTION_THROWN - Class: {}, Method: {}, Username: {}, URI: {}, ClientIP: {}, Exception: {}, Message: {}", 
                    className, methodName, username, uri, clientIp, 
                    exception.getClass().getSimpleName(), exception.getMessage());
                    
        } catch (Exception e) {
            auditLogger.error("Could not log exception details for {}.{}: Original exception: {}, Logging exception: {}", 
                    className, methodName, exception.getMessage(), e.getMessage());
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

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes requestAttributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return requestAttributes != null ? requestAttributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
