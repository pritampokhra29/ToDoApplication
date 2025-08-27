package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.constants.Priority;
import com.example.demo.constants.Status;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating tasks with collaborators in a single request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    
    private String title;
    
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    
    private Status status;
    
    private String category;
    
    private Priority priority;
    
    // Collaborator usernames to add to the task
    private List<String> collaboratorUsernames;
    
    // Alternative: Collaborator user IDs
    private List<Long> collaboratorUserIds;
}
