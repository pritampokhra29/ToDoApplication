package com.example.demo.dto;

public class UserUpdateDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean isActive;
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
