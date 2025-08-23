package com.example.demo.service;

import com.example.demo.constants.Priority;
import com.example.demo.entity.Task;

import java.util.List;

public interface TaskService {

    Task createTask(Task task, String username);

    List<Task> getTasksByUser(String username);

    Task getTaskByIdAndUser(Long id, String username);

    Task updateTask(Long id, Task task, String username);

    void deleteTask(Long id, String username);
    
    // New methods for enhanced functionality
    Task addCollaborator(Long taskId, String taskOwnerUsername, String collaboratorUsername);
    
    Task removeCollaborator(Long taskId, String taskOwnerUsername, String collaboratorUsername);
    
    List<Task> getTasksByUserAndStatus(String username, String status);
    
    List<Task> getTasksByUserAndCategory(String username, String category);
    
    List<Task> getTasksByUserAndPriority(String username, Priority priority);
}
