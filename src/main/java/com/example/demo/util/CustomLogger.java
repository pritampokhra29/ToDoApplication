package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Custom Logger utility class for consistent logging across the application.
 * Provides convenience methods for different types of logging with MDC support.
 */
public class CustomLogger {

    private final Logger logger;
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger methodLogger = LoggerFactory.getLogger("METHOD_EXECUTION");

    private CustomLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static CustomLogger getLogger(Class<?> clazz) {
        return new CustomLogger(clazz);
    }

    // Standard logging methods
    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    public void error(String message, Throwable throwable, Object... args) {
        logger.error(message, throwable);
    }

    // Audit logging methods
    public void auditInfo(String message, Object... args) {
        auditLogger.info(message, args);
    }

    public void auditWarn(String message, Object... args) {
        auditLogger.warn(message, args);
    }

    public void auditError(String message, Object... args) {
        auditLogger.error(message, args);
    }

    // Method execution logging
    public void methodStart(String methodName, Object... args) {
        methodLogger.info("Method started: {} with args: {}", methodName, args);
    }

    public void methodEnd(String methodName, long executionTime) {
        methodLogger.info("Method completed: {} in {}ms", methodName, executionTime);
    }

    public void methodError(String methodName, Throwable throwable, long executionTime) {
        methodLogger.error("Method failed: {} after {}ms - {}", methodName, executionTime, throwable.getMessage());
    }

    // Business operation logging with context
    public void logBusinessOperation(String operation, String entityType, String entityId, String action, String result) {
        try {
            MDC.put("operation", operation);
            MDC.put("entityType", entityType);
            MDC.put("entityId", entityId);
            MDC.put("action", action);
            
            auditLogger.info("BUSINESS_OPERATION - Operation: {}, EntityType: {}, EntityId: {}, Action: {}, Result: {}", 
                    operation, entityType, entityId, action, result);
        } finally {
            MDC.remove("operation");
            MDC.remove("entityType");
            MDC.remove("entityId");
            MDC.remove("action");
        }
    }

    // User activity logging
    public void logUserActivity(String username, String activity, String resource, String details) {
        try {
            MDC.put("activity", activity);
            MDC.put("resource", resource);
            
            auditLogger.info("USER_ACTIVITY - Username: {}, Activity: {}, Resource: {}, Details: {}", 
                    username, activity, resource, details);
        } finally {
            MDC.remove("activity");
            MDC.remove("resource");
        }
    }

    // Security related logging
    public void logSecurityEvent(String eventType, String username, String details, String severity) {
        try {
            MDC.put("eventType", eventType);
            MDC.put("severity", severity);
            
            if ("HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity)) {
                auditLogger.error("SECURITY_EVENT - Type: {}, Username: {}, Details: {}, Severity: {}", 
                        eventType, username, details, severity);
            } else {
                auditLogger.warn("SECURITY_EVENT - Type: {}, Username: {}, Details: {}, Severity: {}", 
                        eventType, username, details, severity);
            }
        } finally {
            MDC.remove("eventType");
            MDC.remove("severity");
        }
    }

    // Performance logging
    public void logPerformance(String operation, long executionTime, String status) {
        try {
            MDC.put("performance", "true");
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", status);
            
            if (executionTime > 5000) { // Log as warning if operation takes more than 5 seconds
                logger.warn("PERFORMANCE - Operation: {} took {}ms - Status: {}", operation, executionTime, status);
            } else {
                logger.info("PERFORMANCE - Operation: {} took {}ms - Status: {}", operation, executionTime, status);
            }
        } finally {
            MDC.remove("performance");
            MDC.remove("executionTime");
            MDC.remove("status");
        }
    }

    // Data change logging
    public void logDataChange(String entityType, String entityId, String changeType, String oldValue, String newValue) {
        try {
            MDC.put("dataChange", "true");
            MDC.put("changeType", changeType);
            
            auditLogger.info("DATA_CHANGE - EntityType: {}, EntityId: {}, ChangeType: {}, OldValue: {}, NewValue: {}", 
                    entityType, entityId, changeType, oldValue, newValue);
        } finally {
            MDC.remove("dataChange");
            MDC.remove("changeType");
        }
    }

    // Exception logging with context
    public void logException(String operation, Throwable throwable, String context) {
        try {
            MDC.put("exception", "true");
            MDC.put("operation", operation);
            MDC.put("context", context);
            
            logger.error("EXCEPTION - Operation: {}, Context: {}, Exception: {}, Message: {}", 
                    operation, context, throwable.getClass().getSimpleName(), throwable.getMessage(), throwable);
        } finally {
            MDC.remove("exception");
            MDC.remove("operation");
            MDC.remove("context");
        }
    }
}
