package com.example.demo.controller;

import com.example.demo.entity.Task;
import com.example.demo.service.TaskService;
import com.example.demo.util.CustomLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private static final CustomLogger logger = CustomLogger.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    // Create a new task
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task, Authentication authentication) {
        String username = authentication.getName();
        
        logger.logUserActivity(username, "CREATE_TASK", "/tasks", "Creating new task: " + task.getTitle());
        logger.logBusinessOperation("CREATE_TASK", "Task", null, "CREATE", "INITIATED");
        
        Task createdTask = taskService.createTask(task, username);
        
        logger.logBusinessOperation("CREATE_TASK", "Task", createdTask.getId().toString(), "CREATE", "SUCCESS");
        logger.logDataChange("Task", createdTask.getId().toString(), "CREATE", null, createdTask.toString());
        
        return ResponseEntity.ok(createdTask);
    }

    // Get all tasks for the logged-in user (including collaborative tasks)
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(Authentication authentication) {
        String username = authentication.getName();
        
        logger.logUserActivity(username, "GET_ALL_TASKS", "/tasks", "Retrieving all tasks for user");
        
        List<Task> tasks = taskService.getTasksByUser(username);
        
        logger.info("Retrieved {} tasks for user: {}", tasks.size(), username);
        
        return ResponseEntity.ok(tasks);
    }

    // Get a specific task by ID using JSON body
    @PostMapping("/get")
    public ResponseEntity<Task> getTaskById(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        Long id = Long.valueOf(request.get("id").toString());
        
        logger.logUserActivity(username, "GET_TASK_BY_ID", "/tasks/get", "Retrieving task with ID: " + id);
        
        Task task = taskService.getTaskByIdAndUser(id, username);
        
        logger.logBusinessOperation("GET_TASK", "Task", id.toString(), "READ", "SUCCESS");
        
        return ResponseEntity.ok(task);
    }

    // GET Search by query parameters 
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasksGet(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            Authentication authentication) {
        
        String username = authentication.getName();
        
        logger.logUserActivity(username, "SEARCH_TASKS_GET", "/tasks/search", 
                "Searching tasks with parameters - title: " + title + ", description: " + description + 
                ", status: " + status + ", priority: " + priority);
        
        // For now, let's implement a simple title search (you can expand this)
        List<Task> tasks;
        if (title != null && !title.trim().isEmpty()) {
            tasks = taskService.searchTasksByKeyword(username, title);
        } else {
            // If no search parameters, return all tasks
            tasks = taskService.getTasksByUser(username);
        }
        
        logger.info("Search returned {} tasks for user: {}", tasks.size(), username);
        
        return ResponseEntity.ok(tasks);
    }

    // GET General filter endpoint with multiple query parameters
    @GetMapping("/filter")
    public ResponseEntity<List<Task>> filterTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            Authentication authentication) {
        
        String username = authentication.getName();
        
        logger.logUserActivity(username, "FILTER_TASKS", "/tasks/filter", 
                "Filtering tasks with parameters - status: " + status + ", priority: " + priority + 
                ", title: " + title + ", description: " + description);
        
        List<Task> tasks;
        
        if (status != null && !status.trim().isEmpty()) {
            tasks = taskService.getTasksByUserAndStatus(username, status);
        } else if (priority != null && !priority.trim().isEmpty()) {
            com.example.demo.constants.Priority priorityEnum = com.example.demo.constants.Priority.valueOf(priority.toUpperCase());
            tasks = taskService.getTasksByUserAndPriority(username, priorityEnum);
        } else if (title != null && !title.trim().isEmpty()) {
            tasks = taskService.searchTasksByKeyword(username, title);
        } else {
            // If no filter parameters, return all tasks
            tasks = taskService.getTasksByUser(username);
        }
        
        logger.info("Filter returned {} tasks for user: {}", tasks.size(), username);
        
        return ResponseEntity.ok(tasks);
    }

    // GET Filter by status using query parameter
    @GetMapping("/filter/status")
    public ResponseEntity<List<Task>> getTasksByStatusGet(
            @RequestParam String status,
            Authentication authentication) {
        
        String username = authentication.getName();
        
        logger.logUserActivity(username, "FILTER_BY_STATUS_GET", "/tasks/filter/status", 
                "Filtering tasks by status: " + status);
        
        List<Task> tasks = taskService.getTasksByUserAndStatus(username, status);
        
        logger.info("Filter by status returned {} tasks for user: {}", tasks.size(), username);
        
        return ResponseEntity.ok(tasks);
    }

    // GET Tasks due today
    @GetMapping("/due-today")
    public ResponseEntity<List<Task>> getTasksDueToday(Authentication authentication) {
        
        String username = authentication.getName();
        LocalDate today = LocalDate.now();
        
        logger.logUserActivity(username, "GET_TASKS_DUE_TODAY", "/tasks/due-today", 
                "Getting tasks due today: " + today);
        
        List<Task> tasks = taskService.getTasksByUserAndDueDate(username, today);
        
        logger.info("Tasks due today returned {} tasks for user: {}", tasks.size(), username);
        
        return ResponseEntity.ok(tasks);
    }

    // GET Filter by priority using query parameter  
    @GetMapping("/filter/priority")
    public ResponseEntity<List<Task>> getTasksByPriorityGet(
            @RequestParam String priority,
            Authentication authentication) {
        
        String username = authentication.getName();
        
        logger.logUserActivity(username, "FILTER_BY_PRIORITY_GET", "/tasks/filter/priority", 
                "Filtering tasks by priority: " + priority);
        
        com.example.demo.constants.Priority priorityEnum = com.example.demo.constants.Priority.valueOf(priority.toUpperCase());
        List<Task> tasks = taskService.getTasksByUserAndPriority(username, priorityEnum);
        
        logger.info("Filter by priority returned {} tasks for user: {}", tasks.size(), username);
        
        return ResponseEntity.ok(tasks);
    }

    // RESTful: Get a specific task by ID using path variable
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskByIdPath(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        
        logger.logUserActivity(username, "GET_TASK_BY_ID", "/tasks/" + id, "Retrieving task with ID: " + id);
        
        Task task = taskService.getTaskByIdAndUser(id, username);
        
        logger.logBusinessOperation("GET_TASK", "Task", id.toString(), "READ", "SUCCESS");
        
        return ResponseEntity.ok(task);
    }

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
            task.setPriority(com.example.demo.constants.Priority.valueOf(request.get("priority").toString()));
        }
        
        Task updatedTask = taskService.updateTask(id, task, username);
        return ResponseEntity.ok(updatedTask);
    }

    // RESTful: Update a task using path variable
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTaskPath(@PathVariable Long id, @RequestBody Task task, Authentication authentication) {
        String username = authentication.getName();
        
        logger.logUserActivity(username, "UPDATE_TASK", "/tasks/" + id, "Updating task with ID: " + id);
        logger.logBusinessOperation("UPDATE_TASK", "Task", id.toString(), "UPDATE", "INITIATED");
        
        Task updatedTask = taskService.updateTask(id, task, username);
        
        logger.logBusinessOperation("UPDATE_TASK", "Task", id.toString(), "UPDATE", "SUCCESS");
        logger.logDataChange("Task", id.toString(), "UPDATE", task.toString(), updatedTask.toString());
        
        return ResponseEntity.ok(updatedTask);
    }
    //     String username = authentication.getName();
    //     Task updatedTask = taskService.updateTask(id, task, username);
    //     return ResponseEntity.ok(updatedTask);
    // }

    // Delete a task using JSON body (soft delete)
    @PostMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteTask(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        Long id = Long.valueOf(request.get("id").toString());
        
        logger.logUserActivity(username, "DELETE_TASK", "/tasks/delete", "Deleting task with ID: " + id);
        logger.logBusinessOperation("DELETE_TASK", "Task", id.toString(), "DELETE", "INITIATED");
        
        taskService.deleteTask(id, username);
        
        logger.logBusinessOperation("DELETE_TASK", "Task", id.toString(), "DELETE", "SUCCESS");
        
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully", "taskId", id.toString()));
    }

    // RESTful: Delete a task using path variable
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTaskPath(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        
        logger.logUserActivity(username, "DELETE_TASK", "/tasks/" + id, "Deleting task with ID: " + id);
        logger.logBusinessOperation("DELETE_TASK", "Task", id.toString(), "DELETE", "INITIATED");
        
        taskService.deleteTask(id, username);
        
        logger.logBusinessOperation("DELETE_TASK", "Task", id.toString(), "DELETE", "SUCCESS");
        
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully", "taskId", id.toString()));
    }

    // Add collaborator to a task
    @PostMapping("/collaborator/add")
    public ResponseEntity<Task> addCollaborator(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        Long taskId = Long.valueOf(request.get("taskId").toString());
        String collaboratorUsername = request.get("collaboratorUsername").toString();
        
        Task updatedTask = taskService.addCollaborator(taskId, username, collaboratorUsername);
        return ResponseEntity.ok(updatedTask);
    }

    // Add collaborator to a task by user ID
    @PostMapping("/collaborator/add/by-id")
    public ResponseEntity<Task> addCollaboratorById(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        Long taskId = Long.valueOf(request.get("taskId").toString());
        Long collaboratorUserId = Long.valueOf(request.get("collaboratorUserId").toString());
        
        Task updatedTask = taskService.addCollaboratorById(taskId, username, collaboratorUserId);
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

    // Get tasks by priority
    @PostMapping("/filter/priority")
    public ResponseEntity<List<Task>> getTasksByPriority(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        String priorityStr = request.get("priority").toString();
        com.example.demo.constants.Priority priority = com.example.demo.constants.Priority.valueOf(priorityStr);
        List<Task> tasks = taskService.getTasksByUserAndPriority(username, priority);
        return ResponseEntity.ok(tasks);
    }

    // **NEW: Pagination and Sorting**
    @GetMapping("/paginated")
    public ResponseEntity<Page<Task>> getTasksWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        
        String username = authentication.getName();
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Task> tasks = taskService.getTasksByUserWithPagination(username, pageable);
        return ResponseEntity.ok(tasks);
    }

    // **NEW: Search by keyword**
    @PostMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        String keyword = request.get("keyword").toString();
        List<Task> tasks = taskService.searchTasksByKeyword(username, keyword);
        return ResponseEntity.ok(tasks);
    }

    // **NEW: Search with pagination**
    @PostMapping("/search/paginated")
    public ResponseEntity<Page<Task>> searchTasksWithPagination(
            @RequestBody Map<String, Object> request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        
        String username = authentication.getName();
        String keyword = request.get("keyword").toString();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Task> tasks = taskService.searchTasksByKeywordWithPagination(username, keyword, pageable);
        return ResponseEntity.ok(tasks);
    }

    // **NEW: Filter by due date**
    @PostMapping("/filter/duedate")
    public ResponseEntity<List<Task>> getTasksByDueDate(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        String dueDateStr = request.get("dueDate").toString();
        LocalDate dueDate = LocalDate.parse(dueDateStr);
        List<Task> tasks = taskService.getTasksByUserAndDueDate(username, dueDate);
        return ResponseEntity.ok(tasks);
    }

    // **NEW: Filter by due date range**
    @PostMapping("/filter/duedate/range")
    public ResponseEntity<List<Task>> getTasksByDueDateRange(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        String startDateStr = request.get("startDate").toString();
        String endDateStr = request.get("endDate").toString();
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        List<Task> tasks = taskService.getTasksByUserAndDueDateRange(username, startDate, endDate);
        return ResponseEntity.ok(tasks);
    }

    // **NEW: Get tasks due before a date**
    @PostMapping("/filter/duedate/before")
    public ResponseEntity<List<Task>> getTasksDueBefore(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        String dateStr = request.get("date").toString();
        LocalDate date = LocalDate.parse(dateStr);
        List<Task> tasks = taskService.getTasksDueBefore(username, date);
        return ResponseEntity.ok(tasks);
    }

    // **NEW: Get tasks due after a date**
    @PostMapping("/filter/duedate/after")
    public ResponseEntity<List<Task>> getTasksDueAfter(@RequestBody Map<String, Object> request, Authentication authentication) {
        String username = authentication.getName();
        String dateStr = request.get("date").toString();
        LocalDate date = LocalDate.parse(dateStr);
        List<Task> tasks = taskService.getTasksDueAfter(username, date);
        return ResponseEntity.ok(tasks);
    }

    // **NEW: Advanced filtering with pagination**
    @GetMapping("/advanced")
    public ResponseEntity<Page<Task>> getTasksWithAdvancedFilters(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dueDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        
        String username = authentication.getName();
        LocalDate dueDateParsed = null;
        if (dueDate != null && !dueDate.isEmpty()) {
            dueDateParsed = LocalDate.parse(dueDate);
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Task> tasks = taskService.getTasksWithFilters(username, status, category, dueDateParsed, pageable);
        return ResponseEntity.ok(tasks);
    }
}
