package com.example.demo.dto;

/**
 * Request object for logout operations
 * Can optionally include refresh token to be blacklisted
 */
public class LogoutRequest {
    
    private String refreshToken;
    
    public LogoutRequest() {
    }
    
    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
