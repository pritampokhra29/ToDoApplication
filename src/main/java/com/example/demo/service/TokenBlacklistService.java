package com.example.demo.service;

import com.example.demo.util.CustomLogger;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage blacklisted JWT tokens
 * Used for logout functionality to invalidate tokens
 */
@Service
public class TokenBlacklistService {

    private static final CustomLogger logger = CustomLogger.getLogger(TokenBlacklistService.class);

    // Thread-safe set to store blacklisted tokens
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    
    // Store token expiration times for cleanup
    private final ConcurrentHashMap<String, Long> tokenExpirations = new ConcurrentHashMap<>();

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Add a token to the blacklist
     */
    public void blacklistToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            blacklistedTokens.add(token);
            
            // Store expiration time for cleanup
            try {
                Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
                if (expirationDate != null) {
                    tokenExpirations.put(token, expirationDate.getTime());
                }
            } catch (Exception e) {
                logger.logException("TOKEN_EXPIRATION_ERROR", e, "Error getting expiration from token for blacklist");
                // Store current time + 24 hours as fallback
                tokenExpirations.put(token, System.currentTimeMillis() + 86400000);
            }
            
            logger.logSecurityEvent("TOKEN_BLACKLISTED", "system", 
                "Token added to blacklist", "MEDIUM");
        }
    }

    /**
     * Check if a token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        boolean isBlacklisted = blacklistedTokens.contains(token);
        
        if (isBlacklisted) {
            logger.logSecurityEvent("BLACKLISTED_TOKEN_ACCESS", "unknown", 
                "Attempt to use blacklisted token", "HIGH");
        }
        
        return isBlacklisted;
    }

    /**
     * Get the count of blacklisted tokens
     */
    public int getBlacklistedTokenCount() {
        return blacklistedTokens.size();
    }

    /**
     * Clean expired tokens from blacklist
     * Runs every hour to prevent memory leaks
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        int initialSize = blacklistedTokens.size();
        
        // Remove expired tokens
        tokenExpirations.entrySet().removeIf(entry -> {
            String token = entry.getKey();
            Long expirationTime = entry.getValue();
            
            if (expirationTime < currentTime) {
                blacklistedTokens.remove(token);
                logger.logBusinessOperation("TOKEN_CLEANUP", "Token", token.substring(0, Math.min(10, token.length())), 
                    "DELETE", "Expired token removed from blacklist");
                return true;
            }
            return false;
        });
        
        int finalSize = blacklistedTokens.size();
        int removedCount = initialSize - finalSize;
        
        if (removedCount > 0) {
            logger.logBusinessOperation("TOKEN_CLEANUP", "System", "Blacklist", "CLEANUP", 
                "Removed " + removedCount + " expired tokens from blacklist");
        }
        
        logger.info("Token blacklist cleanup completed. Removed {} expired tokens. Current blacklist size: {}", 
            removedCount, finalSize);
    }

    /**
     * Clear all blacklisted tokens (for testing or admin purposes)
     */
    public void clearAllBlacklistedTokens() {
        int tokenCount = blacklistedTokens.size();
        blacklistedTokens.clear();
        tokenExpirations.clear();
        
        logger.logSecurityEvent("BLACKLIST_CLEARED", "admin", 
            "All blacklisted tokens cleared: " + tokenCount + " tokens", "LOW");
    }
}
