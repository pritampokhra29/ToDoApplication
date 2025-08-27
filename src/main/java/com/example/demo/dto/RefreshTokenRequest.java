package com.example.demo.dto;

public class RefreshTokenRequest {
    
    private String refreshToken;

    // Default constructor
    public RefreshTokenRequest() {}

    // Constructor
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
                "refreshToken='[HIDDEN]'" +
                '}';
    }
}
