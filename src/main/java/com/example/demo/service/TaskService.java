package com.example.demo.service;

import com.example.demo.constants.Priority;
import com.example.demo.entity.Task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    Task createTask(Task task, String username);

    List<Task> getTasksByUser(String username);

    Task getTaskByIdAndUser(Long id, String username);

    Task updateTask(Long id, Task task, String username);

    void deleteTask(Long id, String username);
    
    // Existing enhanced functionality
    Task addCollaborator(Long taskId, String taskOwnerUsername, String collaboratorUsername);
    
    Task removeCollaborator(Long taskId, String taskOwnerUsername, String collaboratorUsername);
    
    List<Task> getTasksByUserAndStatus(String username, String status);
    
    List<Task> getTasksByUserAndCategory(String username, String category);
    
    List<Task> getTasksByUserAndPriority(String username, Priority priority);
    
    // **NEW: Pagination and Sorting**
    Page<Task> getTasksByUserWithPagination(String username, Pageable pageable);
    
    // **NEW: Search functionality**
    List<Task> searchTasksByKeyword(String username, String keyword);
    Page<Task> searchTasksByKeywordWithPagination(String username, String keyword, Pageable pageable);
    
    // **NEW: Due date filtering**
    List<Task> getTasksByUserAndDueDate(String username, LocalDate dueDate);
    List<Task> getTasksByUserAndDueDateRange(String username, LocalDate startDate, LocalDate endDate);
    List<Task> getTasksDueBefore(String username, LocalDate date);
    List<Task> getTasksDueAfter(String username, LocalDate date);
    
    // **NEW: Advanced filtering with pagination**
    Page<Task> getTasksWithFilters(String username, String status, String category, LocalDate dueDate, Pageable pageable);
}
