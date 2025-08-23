package com.example.demo.service.impl;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.repo.TaskRepo;
import com.example.demo.repo.UserRepo;
import com.example.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private UserRepo userRepo;

    @Override
    public Task createTask(Task task, String username) {
        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        
        // Set owner and timestamps
        task.setUser(user);
        task.setCreateDate(LocalDate.now());
        task.setUpdateDate(LocalDate.now());
        task.setDeleted(false);
        
        // Initialize collaborators if not set
        if (task.getCollaborators() == null) {
            task.setCollaborators(new HashSet<>());
        }
        
        // Add the creator as a collaborator by default
        task.getCollaborators().add(user);
        
        return taskRepo.save(task);
    }

    @Override
    public List<Task> getTasksByUser(String username) {
        // Get tasks where user is owner and not deleted
        List<Task> ownedTasks = taskRepo.findByUserUsernameAndDeletedFalse(username);
        
        // Get all non-deleted tasks to check for collaborations
        List<Task> allTasks = taskRepo.findAllByDeletedFalse();
        
        // Find tasks where user is a collaborator
        List<Task> collaborativeTasks = allTasks.stream()
            .filter(task -> !task.getUser().getUsername().equals(username)) // Not owned by user
            .filter(task -> task.getCollaborators() != null && 
                          task.getCollaborators().stream()
                              .anyMatch(collaborator -> collaborator.getUsername().equals(username)))
            .collect(java.util.stream.Collectors.toList());
        
        // Combine owned and collaborative tasks
        ownedTasks.addAll(collaborativeTasks);
        return ownedTasks;
    }

    @Override
    public Task getTaskByIdAndUser(Long id, String username) {
        // Check if user is owner
        Optional<Task> ownedTask = taskRepo.findByIdAndUserUsernameAndDeletedFalse(id, username);
        if (ownedTask.isPresent()) {
            return ownedTask.get();
        }
        
        // If not owner, check if user is collaborator
        Optional<Task> taskById = taskRepo.findByIdAndDeletedFalse(id);
        if (taskById.isPresent()) {
            Task foundTask = taskById.get();
            if (foundTask.getCollaborators() != null) {
                boolean isCollaborator = foundTask.getCollaborators().stream()
                    .anyMatch(user -> user.getUsername().equals(username));
                if (isCollaborator) {
                    return foundTask;
                }
            }
        }
        
        throw new RuntimeException("Task not found or access denied");
    }

    @Override
    public Task updateTask(Long id, Task task, String username) {
        Task existingTask = getTaskByIdAndUser(id, username);
        
        // Update basic fields
        if (task.getTitle() != null) {
            existingTask.setTitle(task.getTitle());
        }
        if (task.getDescription() != null) {
            existingTask.setDescription(task.getDescription());
        }
        if (task.getDueDate() != null) {
            existingTask.setDueDate(task.getDueDate());
        }
        if (task.getStatus() != null) {
            existingTask.setStatus(task.getStatus());
            // Set completion date if task is completed
            if (task.getStatus().toString().equals("COMPLETED")) {
                existingTask.setCompletionDate(LocalDate.now());
            }
        }
        if (task.getCategory() != null) {
            existingTask.setCategory(task.getCategory());
        }
        if (task.getPriority() != null) {
            existingTask.setPriority(task.getPriority());
        }
        
        // Update collaborators if provided
        if (task.getCollaborators() != null) {
            existingTask.setCollaborators(task.getCollaborators());
        }
        
        // Always update the timestamp
        existingTask.setUpdateDate(LocalDate.now());
        
        return taskRepo.save(existingTask);
    }

    @Override
    public void deleteTask(Long id, String username) {
        Task task = getTaskByIdAndUser(id, username);
        
        // Soft delete - mark as deleted instead of removing
        task.setDeleted(true);
        task.setUpdateDate(LocalDate.now());
        taskRepo.save(task);
    }
    
    // New method to add collaborator
    public Task addCollaborator(Long taskId, String taskOwnerUsername, String collaboratorUsername) {
        Task task = getTaskByIdAndUser(taskId, taskOwnerUsername);
        User collaborator = userRepo.findByUsername(collaboratorUsername)
                .orElseThrow(() -> new RuntimeException("Collaborator user not found"));
        
        if (task.getCollaborators() == null) {
            task.setCollaborators(new HashSet<>());
        }
        
        task.getCollaborators().add(collaborator);
        task.setUpdateDate(LocalDate.now());
        
        return taskRepo.save(task);
    }
    
    // New method to remove collaborator
    public Task removeCollaborator(Long taskId, String taskOwnerUsername, String collaboratorUsername) {
        Task task = getTaskByIdAndUser(taskId, taskOwnerUsername);
        User collaborator = userRepo.findByUsername(collaboratorUsername)
                .orElseThrow(() -> new RuntimeException("Collaborator user not found"));
        
        if (task.getCollaborators() != null) {
            task.getCollaborators().remove(collaborator);
            task.setUpdateDate(LocalDate.now());
        }
        
        return taskRepo.save(task);
    }
    
    // Enhanced method to get tasks by status including collaborator access
    public List<Task> getTasksByUserAndStatus(String username, String status) {
        // Get owned tasks with specific status
        List<Task> ownedTasks = taskRepo.findByUserUsernameAndDeletedFalse(username).stream()
            .filter(task -> task.getStatus().toString().equals(status))
            .collect(java.util.stream.Collectors.toList());
        
        // Get all tasks with status to check for collaborations
        List<Task> allTasks = taskRepo.findAllByDeletedFalse();
        
        // Find tasks where user is a collaborator with specific status
        List<Task> collaborativeTasks = allTasks.stream()
            .filter(task -> !task.getUser().getUsername().equals(username)) // Not owned by user
            .filter(task -> task.getStatus().toString().equals(status)) // Has the required status
            .filter(task -> task.getCollaborators() != null && 
                          task.getCollaborators().stream()
                              .anyMatch(collaborator -> collaborator.getUsername().equals(username)))
            .collect(java.util.stream.Collectors.toList());
        
        // Combine owned and collaborative tasks
        ownedTasks.addAll(collaborativeTasks);
        return ownedTasks;
    }
    
    // Enhanced method to get tasks by category including collaborator access
    public List<Task> getTasksByUserAndCategory(String username, String category) {
        // Get owned tasks with specific category
        List<Task> ownedTasks = taskRepo.findByUserUsernameAndDeletedFalse(username).stream()
            .filter(task -> category.equals(task.getCategory()))
            .collect(java.util.stream.Collectors.toList());
        
        // Get all tasks with category to check for collaborations
        List<Task> allTasks = taskRepo.findAllByDeletedFalse();
        
        // Find tasks where user is a collaborator with specific category
        List<Task> collaborativeTasks = allTasks.stream()
            .filter(task -> !task.getUser().getUsername().equals(username)) // Not owned by user
            .filter(task -> category.equals(task.getCategory())) // Has the required category
            .filter(task -> task.getCollaborators() != null && 
                          task.getCollaborators().stream()
                              .anyMatch(collaborator -> collaborator.getUsername().equals(username)))
            .collect(java.util.stream.Collectors.toList());
        
        // Combine owned and collaborative tasks
        ownedTasks.addAll(collaborativeTasks);
        return ownedTasks;
    }
    
    // Enhanced method to get tasks by priority including collaborator access
    public List<Task> getTasksByUserAndPriority(String username, com.example.demo.constants.Priority priority) {
        // Get owned tasks with specific priority
        List<Task> ownedTasks = taskRepo.findByUserUsernameAndDeletedFalse(username).stream()
            .filter(task -> priority.equals(task.getPriority()))
            .collect(java.util.stream.Collectors.toList());
        
        // Get all tasks to check for collaborations
        List<Task> allTasks = taskRepo.findAllByDeletedFalse();
        
        // Find tasks where user is a collaborator with specific priority
        List<Task> collaborativeTasks = allTasks.stream()
            .filter(task -> !task.getUser().getUsername().equals(username)) // Not owned by user
            .filter(task -> priority.equals(task.getPriority())) // Has the required priority
            .filter(task -> task.getCollaborators() != null && 
                          task.getCollaborators().stream()
                              .anyMatch(collaborator -> collaborator.getUsername().equals(username)))
            .collect(java.util.stream.Collectors.toList());
        
        // Combine owned and collaborative tasks
        ownedTasks.addAll(collaborativeTasks);
        return ownedTasks;
    }
}
