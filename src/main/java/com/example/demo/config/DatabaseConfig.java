package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Database configuration properties that are externalized for security.
 * This class binds environment variables to configuration properties.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class DatabaseConfig {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    
    private Hikari hikari = new Hikari();
    
    @Data
    public static class Hikari {
        private int maximumPoolSize = 20;
        private int minimumIdle = 5;
        private long connectionTimeout = 20000;
        private long idleTimeout = 300000;
        private long leakDetectionThreshold = 60000;
    }
    
    /**
     * Validates that required database properties are set
     */
    public void validate() {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("Database URL is required. Set DATABASE_URL environment variable.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("Database username is required. Set DATABASE_USERNAME environment variable.");
        }
        if (password == null) {
            throw new IllegalStateException("Database password is required. Set DATABASE_PASSWORD environment variable.");
        }
    }
    
    /**
     * Get database type from URL
     */
    public String getDatabaseType() {
        if (url == null) return "unknown";
        
        if (url.contains("postgresql")) {
            return "postgresql";
        } else if (url.contains("h2")) {
            return "h2";
        } else if (url.contains("mysql")) {
            return "mysql";
        }
        return "unknown";
    }
    
    /**
     * Check if database is PostgreSQL
     */
    public boolean isPostgreSQL() {
        return "postgresql".equals(getDatabaseType());
    }
    
    /**
     * Check if database is H2
     */
    public boolean isH2() {
        return "h2".equals(getDatabaseType());
    }
}
