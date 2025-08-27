package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.constants.Priority;
import com.example.demo.constants.Status;
import com.example.demo.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for task responses that includes collaborator information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    private Long id;
    
    private String title;
    
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    
    private Status status;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate updateDate;
    
    private Boolean deleted;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate completionDate;
    
    private String category;
    
    private Priority priority;
    
    // Task owner
    private UserDTO owner;
    
    // List of collaborators (without sensitive information)
    private List<UserDTO> collaborators;
    
    /**
     * Simplified user information for responses
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String username;
        private String email;
        private String role;
        private boolean isActive;
        
        public static UserDTO fromUser(User user) {
            return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
            );
        }
    }
}
