package com.example.demo.test;

import com.example.demo.util.CustomLogger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Test component to demonstrate logging functionality.
 * This will run on application startup to show various logging features.
 */
@Component
public class LoggingTestRunner implements CommandLineRunner {

    private static final CustomLogger logger = CustomLogger.getLogger(LoggingTestRunner.class);

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Logging System Test Started ===");
        
        // Test basic logging
        logger.info("Testing basic info logging");
        logger.debug("Testing debug logging");
        logger.warn("Testing warning logging");
        
        // Test audit logging
        logger.auditInfo("Testing audit logging functionality");
        logger.logUserActivity("system", "STARTUP", "APPLICATION", "Application startup test");
        
        // Test business operation logging
        logger.logBusinessOperation("TEST_OPERATION", "TestEntity", "TEST123", "READ", "SUCCESS");
        
        // Test performance logging
        logger.logPerformance("STARTUP_TEST", 100, "SUCCESS");
        
        // Test security event logging
        logger.logSecurityEvent("SYSTEM_STARTUP", "system", "Application started successfully", "LOW");
        
        // Test data change logging
        logger.logDataChange("TestEntity", "TEST123", "CREATE", null, "test-value");
        
        // Test method execution logging
        logger.methodStart("testMethod", "arg1", "arg2");
        simulateMethod();
        logger.methodEnd("testMethod", 50);
        
        logger.info("=== Logging System Test Completed ===");
    }
    
    private void simulateMethod() {
        try {
            Thread.sleep(50); // Simulate some work
            logger.info("Simulated method execution completed");
        } catch (InterruptedException e) {
            logger.logException("SIMULATE_METHOD", e, "Thread interrupted during simulation");
            Thread.currentThread().interrupt();
        }
    }
}
