package com.example.demo.service;

import com.example.demo.constants.Priority;
import com.example.demo.entity.Task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.dto.CreateTaskRequest;
import com.example.demo.dto.TaskResponse;
import com.example.demo.dto.UpdateTaskRequest;

public interface TaskService {

    Task createTask(Task task, String username);
    
    // **NEW: Create task with collaborators in a single request**
    TaskResponse createTaskWithCollaborators(CreateTaskRequest request, String username);

    List<Task> getTasksByUser(String username);
    
    // **NEW: Get tasks with collaborator details**
    List<TaskResponse> getTasksWithCollaboratorsByUser(String username);

    Task getTaskByIdAndUser(Long id, String username);
    
    // **NEW: Get single task with collaborator details**
    TaskResponse getTaskWithCollaboratorsByIdAndUser(Long id, String username);

    Task updateTask(Long id, Task task, String username);
    
    // **NEW: Update task with collaborators in a single request**
    TaskResponse updateTaskWithCollaborators(UpdateTaskRequest request, String username);

    void deleteTask(Long id, String username);
    
    // Existing enhanced functionality
    Task addCollaborator(Long taskId, String taskOwnerUsername, String collaboratorUsername);
    
    Task addCollaboratorById(Long taskId, String taskOwnerUsername, Long collaboratorUserId);
    
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
