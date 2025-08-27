package com.example.demo.dto;

import jakarta.validation.constraints.*;

public class UserUpdateDTO {
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long id;
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Username can only contain letters, numbers, and underscores")
    private String username;
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Pattern(regexp = "^(USER|ADMIN)?$", message = "Role must be either USER or ADMIN")
    private String role;
    
    private Boolean isActive;
    
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$|^$", 
            message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
    private String password; // Optional - only if admin wants to change password

    // Default constructor
    public UserUpdateDTO() {}

    // Constructor with all fields
    public UserUpdateDTO(Long id, String username, String email, String role, Boolean isActive, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserUpdateDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", password=" + (password != null ? "[PROTECTED]" : "null") +
                '}';
    }
}
