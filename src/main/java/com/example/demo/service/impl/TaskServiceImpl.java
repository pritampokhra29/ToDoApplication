package com.example.demo.service.impl;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.repo.TaskRepo;
import com.example.demo.repo.UserRepo;
import com.example.demo.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

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
        LOGGER.info("Getting tasks for user: {}", username);
        
        // **FIXED: Use database-level query to avoid lazy loading**
        List<Task> userTasks = taskRepo.findTasksByOwnerOrCollaborator(username);
        
        LOGGER.info("Found {} tasks for user {}", userTasks.size(), username);
        return userTasks;
    }

    @Override
    public Task getTaskByIdAndUser(Long id, String username) {
        LOGGER.info("Getting task {} for user: {}", id, username);
        
        // **FIXED: Use database-level query to avoid lazy loading**
        Optional<Task> userTask = taskRepo.findTaskByIdAndOwnerOrCollaborator(id, username);
        
        if (userTask.isPresent()) {
            LOGGER.info("Found task {} for user {}", id, username);
            return userTask.get();
        }
        
        LOGGER.warn("Task {} not found or access denied for user {}", id, username);
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
            // Create a Set to automatically handle duplicates based on User entity equals/hashCode
            // Note: If User entity doesn't override equals/hashCode properly, we'll use a LinkedHashSet
            // and filter by ID to ensure no duplicate user IDs
            Set<User> uniqueCollaborators = new LinkedHashSet<>();
            Set<Long> addedUserIds = new HashSet<>();
            
            for (User collaborator : task.getCollaborators()) {
                if (collaborator != null && collaborator.getId() != null) {
                    if (!addedUserIds.contains(collaborator.getId())) {
                        uniqueCollaborators.add(collaborator);
                        addedUserIds.add(collaborator.getId());
                    }
                }
            }
            
            existingTask.setCollaborators(uniqueCollaborators);
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
        
        // Check if collaborator is already added using database query
        boolean isAlreadyCollaborator = taskRepo.existsCollaboratorByTaskIdAndUserId(taskId, collaborator.getId());
        
        if (isAlreadyCollaborator) {
            throw new RuntimeException("User '" + collaboratorUsername + "' is already a collaborator on this task");
        }
        
        if (task.getCollaborators() == null) {
            task.setCollaborators(new HashSet<>());
        }
        
        task.getCollaborators().add(collaborator);
        task.setUpdateDate(LocalDate.now());
        
        return taskRepo.save(task);
    }
    
    // New method to add collaborator by user ID
    public Task addCollaboratorById(Long taskId, String taskOwnerUsername, Long collaboratorUserId) {
        Task task = getTaskByIdAndUser(taskId, taskOwnerUsername);
        User collaborator = userRepo.findById(collaboratorUserId)
                .orElseThrow(() -> new RuntimeException("Collaborator user with ID " + collaboratorUserId + " not found"));
        
        // Check if collaborator is already added using database query
        boolean isAlreadyCollaborator = taskRepo.existsCollaboratorByTaskIdAndUserId(taskId, collaboratorUserId);
        
        if (isAlreadyCollaborator) {
            throw new RuntimeException("User with ID " + collaboratorUserId + " ('" + collaborator.getUsername() + "') is already a collaborator on this task");
        }
        
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
    
    // **NEW: Pagination and Sorting methods**
    @Override
    public org.springframework.data.domain.Page<Task> getTasksByUserWithPagination(String username, org.springframework.data.domain.Pageable pageable) {
        // For now, return paginated owned tasks only (can be enhanced to include collaborators)
        return taskRepo.findByUserUsernameAndDeletedFalse(username, pageable);
    }
    
    // **NEW: Search functionality**
    @Override
    public List<Task> searchTasksByKeyword(String username, String keyword) {
        // Get owned tasks matching keyword
        List<Task> ownedTasks = taskRepo.findByUserUsernameAndKeywordInTitleOrDescription(username, keyword);
        
        // TODO: Add collaborator search logic if needed
        return ownedTasks;
    }
    
    @Override
    public org.springframework.data.domain.Page<Task> searchTasksByKeywordWithPagination(String username, String keyword, org.springframework.data.domain.Pageable pageable) {
        return taskRepo.findByUserUsernameAndKeywordInTitleOrDescription(username, keyword, pageable);
    }
    
    // **NEW: Due date filtering**
    @Override
    public List<Task> getTasksByUserAndDueDate(String username, java.time.LocalDate dueDate) {
        // Get owned tasks with specific due date
        List<Task> ownedTasks = taskRepo.findByUserUsernameAndDueDateAndDeletedFalse(username, dueDate);
        
        // Get all tasks with due date to check for collaborations
        List<Task> allTasks = taskRepo.findAllByDeletedFalse();
        
        // Find tasks where user is a collaborator with specific due date
        List<Task> collaborativeTasks = allTasks.stream()
            .filter(task -> !task.getUser().getUsername().equals(username)) // Not owned by user
            .filter(task -> dueDate.equals(task.getDueDate())) // Has the required due date
            .filter(task -> task.getCollaborators() != null && 
                          task.getCollaborators().stream()
                              .anyMatch(collaborator -> collaborator.getUsername().equals(username)))
            .collect(java.util.stream.Collectors.toList());
        
        // Combine owned and collaborative tasks
        ownedTasks.addAll(collaborativeTasks);
        return ownedTasks;
    }
    
    @Override
    public List<Task> getTasksByUserAndDueDateRange(String username, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<Task> ownedTasks = taskRepo.findByUserUsernameAndDueDateBetweenAndDeletedFalse(username, startDate, endDate);
        
        // TODO: Add collaborator logic for date range if needed
        return ownedTasks;
    }
    
    @Override
    public List<Task> getTasksDueBefore(String username, java.time.LocalDate date) {
        return taskRepo.findByUserUsernameAndDueDateBeforeAndDeletedFalse(username, date);
    }
    
    @Override
    public List<Task> getTasksDueAfter(String username, java.time.LocalDate date) {
        return taskRepo.findByUserUsernameAndDueDateAfterAndDeletedFalse(username, date);
    }
    
    // **NEW: Advanced filtering with pagination**
    @Override
    public org.springframework.data.domain.Page<Task> getTasksWithFilters(String username, String status, String category, java.time.LocalDate dueDate, org.springframework.data.domain.Pageable pageable) {
        com.example.demo.constants.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = com.example.demo.constants.Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        return taskRepo.findTasksWithFilters(username, statusEnum, category, dueDate, pageable);
    }
}
