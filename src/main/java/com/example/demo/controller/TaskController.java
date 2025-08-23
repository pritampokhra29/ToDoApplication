package com.example.demo.controller;

import com.example.demo.entity.Task;
import com.example.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Create a new task
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task, Authentication authentication) {
        String username = authentication.getName();
        Task createdTask = taskService.createTask(task, username);
        return ResponseEntity.ok(createdTask);
    }

    // Get all tasks for the logged-in user (including collaborative tasks)
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(Authentication authentication) {
        String username = authentication.getName();
        List<Task> tasks = taskService.getTasksByUser(username);
        return ResponseEntity.ok(tasks);
    }

    // Get a specific task by ID using JSON body
    @PostMapping("/get")
    public ResponseEntity<Task> getTaskById(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        Long id = Long.valueOf(request.get("id").toString());
        Task task = taskService.getTaskByIdAndUser(id, username);
        return ResponseEntity.ok(task);
    }

    // // Legacy: Get a specific task by ID using path variable (keeping for backward compatibility)
    // @GetMapping("/{id}")
    // public ResponseEntity<Task> getTaskByIdPath(@PathVariable Long id, Authentication authentication) {
    //     String username = authentication.getName();
    //     Task task = taskService.getTaskByIdAndUser(id, username);
    //     return ResponseEntity.ok(task);
    // }

    // Update a task using JSON body (includes task ID)
    @PostMapping("/update")
    public ResponseEntity<Task> updateTask(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        Long id = Long.valueOf(request.get("id").toString());
        
        // Create task object from request data
        Task task = new Task();
        if (request.containsKey("title")) {
            task.setTitle(request.get("title").toString());
        }
        if (request.containsKey("description")) {
            task.setDescription(request.get("description").toString());
        }
        if (request.containsKey("dueDate")) {
            task.setDueDate(java.time.LocalDate.parse(request.get("dueDate").toString()));
        }
        if (request.containsKey("status")) {
            task.setStatus(com.example.demo.constants.Status.valueOf(request.get("status").toString()));
        }
        if (request.containsKey("category")) {
            task.setCategory(request.get("category").toString());
        }
        if (request.containsKey("priority")) {
            task.setPriority(request.get("priority").toString());
        }
        
        Task updatedTask = taskService.updateTask(id, task, username);
        return ResponseEntity.ok(updatedTask);
    }

    // // Legacy: Update a task using path variable (keeping for backward compatibility)
    // @PutMapping("/{id}")
    // public ResponseEntity<Task> updateTaskPath(@PathVariable Long id, @RequestBody Task task, Authentication authentication) {
    //     String username = authentication.getName();
    //     Task updatedTask = taskService.updateTask(id, task, username);
    //     return ResponseEntity.ok(updatedTask);
    // }

    // Delete a task using JSON body (soft delete)
    @PostMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteTask(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        Long id = Long.valueOf(request.get("id").toString());
        taskService.deleteTask(id, username);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully", "taskId", id.toString()));
    }

    // // Legacy: Delete a task using path variable (keeping for backward compatibility)
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteTaskPath(@PathVariable Long id, Authentication authentication) {
    //     String username = authentication.getName();
    //     taskService.deleteTask(id, username);
    //     return ResponseEntity.noContent().build();
    // }

    // Add collaborator to a task
    @PostMapping("/collaborator/add")
    public ResponseEntity<Task> addCollaborator(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        Long taskId = Long.valueOf(request.get("taskId").toString());
        String collaboratorUsername = request.get("collaboratorUsername").toString();
        
        Task updatedTask = taskService.addCollaborator(taskId, username, collaboratorUsername);
        return ResponseEntity.ok(updatedTask);
    }

    // Remove collaborator from a task
    @PostMapping("/collaborator/remove")
    public ResponseEntity<Task> removeCollaborator(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        Long taskId = Long.valueOf(request.get("taskId").toString());
        String collaboratorUsername = request.get("collaboratorUsername").toString();
        
        Task updatedTask = taskService.removeCollaborator(taskId, username, collaboratorUsername);
        return ResponseEntity.ok(updatedTask);
    }

    // Get tasks by status
    @PostMapping("/filter/status")
    public ResponseEntity<List<Task>> getTasksByStatus(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        String status = request.get("status").toString();
        List<Task> tasks = taskService.getTasksByUserAndStatus(username, status);
        return ResponseEntity.ok(tasks);
    }

    // Get tasks by category
    @PostMapping("/filter/category")
    public ResponseEntity<List<Task>> getTasksByCategory(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        String category = request.get("category").toString();
        List<Task> tasks = taskService.getTasksByUserAndCategory(username, category);
        return ResponseEntity.ok(tasks);
    }
}
