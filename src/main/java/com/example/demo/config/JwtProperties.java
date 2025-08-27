package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties
 * Handles JWT-related configuration from application.properties
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    private boolean enabled = true; // Default to JWT enabled
    private String secret;
    private long expiration = 86400000; // 24 hours in milliseconds
    private Refresh refresh = new Refresh();
    
    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
    
    public Refresh getRefresh() {
        return refresh;
    }
    
    public void setRefresh(Refresh refresh) {
        this.refresh = refresh;
    }
    
    public static class Refresh {
        private long expiration = 604800000; // 7 days in milliseconds
        
        public long getExpiration() {
            return expiration;
        }
        
        public void setExpiration(long expiration) {
            this.expiration = expiration;
        }
    }
}
