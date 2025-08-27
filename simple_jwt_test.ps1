# Simple JWT TodoList API Test Script
# Tests key endpoints including authentication, task CRUD, and token management

Write-Host "=== JWT TodoList API Test Script ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"

# Test 1: Admin Login
Write-Host "Test 1: Admin login..." -ForegroundColor Yellow
try {
    $loginBody = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $loginData = $loginResponse.Content | ConvertFrom-Json
    
    Write-Host "SUCCESS: Admin login successful" -ForegroundColor Green
    Write-Host "  Username: $($loginData.username)" -ForegroundColor Cyan
    Write-Host "  Roles: $($loginData.roles -join ', ')" -ForegroundColor Cyan
    
    $adminToken = $loginData.accessToken
    $adminRefreshToken = $loginData.refreshToken
    
} catch {
    Write-Host "FAILED: Admin login failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: User Registration with Admin Token
Write-Host "Test 2: Testing user registration with admin token..." -ForegroundColor Yellow
try {
    $regBody = @{
        username = "testuser_$(Get-Random)"
        password = "password123"
        email = "testuser@example.com"
        role = "USER"
    } | ConvertTo-Json
    
    $regResponse = Invoke-WebRequest -Uri "$baseUrl/auth/register" -Method POST -Body $regBody -ContentType "application/json" -Headers @{Authorization="Bearer $adminToken"}
    Write-Host "SUCCESS: User registration successful" -ForegroundColor Green
    Write-Host "  Status Code: $($regResponse.StatusCode)" -ForegroundColor Cyan
    Write-Host "  Response: $($regResponse.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "FAILED: User registration failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorContent = $reader.ReadToEnd()
            Write-Host "  Error Content: $errorContent" -ForegroundColor Red
        } catch {
            Write-Host "  Could not read error response" -ForegroundColor Red
        }
    }
}
Write-Host ""

# Test 3: Regular User Login
Write-Host "Test 3: Regular user login..." -ForegroundColor Yellow
try {
    $userLoginBody = @{
        username = "john"
        password = "password123"
    } | ConvertTo-Json
    
    $userLoginResponse = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $userLoginBody -ContentType "application/json"
    $userLoginData = $userLoginResponse.Content | ConvertFrom-Json
    
    Write-Host "SUCCESS: User login successful" -ForegroundColor Green
    Write-Host "  Username: $($userLoginData.username)" -ForegroundColor Cyan
    Write-Host "  Roles: $($userLoginData.roles -join ', ')" -ForegroundColor Cyan
    
    $userToken = $userLoginData.accessToken
    $userRefreshToken = $userLoginData.refreshToken
    
} catch {
    Write-Host "FAILED: User login failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 4: Create Task
Write-Host "Test 4: Creating a new task..." -ForegroundColor Yellow
try {
    $taskBody = @{
        title = "Test Task from PowerShell Script"
        description = "This is a test task created via PowerShell API test"
        priority = "HIGH"
        status = "PENDING"
        category = "Work"
        dueDate = "2025-08-30"
    } | ConvertTo-Json
    
    $taskResponse = Invoke-WebRequest -Uri "$baseUrl/tasks" -Method POST -Body $taskBody -ContentType "application/json" -Headers @{Authorization="Bearer $userToken"}
    $task = $taskResponse.Content | ConvertFrom-Json
    
    Write-Host "SUCCESS: Task creation successful" -ForegroundColor Green
    Write-Host "  Task ID: $($task.id)" -ForegroundColor Cyan
    Write-Host "  Task Title: $($task.title)" -ForegroundColor Cyan
    
    $taskId = $task.id
    
} catch {
    Write-Host "FAILED: Task creation failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 5: Get Task by ID (RESTful endpoint)
Write-Host "Test 5: Getting task by ID using RESTful endpoint..." -ForegroundColor Yellow
try {
    $getTaskResponse = Invoke-WebRequest -Uri "$baseUrl/tasks/$taskId" -Method GET -Headers @{Authorization="Bearer $userToken"}
    $retrievedTask = $getTaskResponse.Content | ConvertFrom-Json
    
    Write-Host "SUCCESS: Get task by ID successful" -ForegroundColor Green
    Write-Host "  Retrieved Task: $($retrievedTask.title)" -ForegroundColor Cyan
    Write-Host "  Task Status: $($retrievedTask.status)" -ForegroundColor Cyan
    
} catch {
    Write-Host "FAILED: Get task by ID failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 6: Update Task by ID (RESTful endpoint)
Write-Host "Test 6: Updating task by ID using RESTful endpoint..." -ForegroundColor Yellow
try {
    $updateBody = @{
        title = "Updated Task Title via PowerShell"
        description = "This task has been updated via PowerShell API test"
        priority = "MEDIUM"
        status = "IN_PROGRESS"
        category = "Work"
        dueDate = "2025-08-31"
    } | ConvertTo-Json
    
    $updateResponse = Invoke-WebRequest -Uri "$baseUrl/tasks/$taskId" -Method PUT -Body $updateBody -ContentType "application/json" -Headers @{Authorization="Bearer $userToken"}
    $updatedTask = $updateResponse.Content | ConvertFrom-Json
    
    Write-Host "SUCCESS: Update task by ID successful" -ForegroundColor Green
    Write-Host "  Updated Task: $($updatedTask.title)" -ForegroundColor Cyan
    Write-Host "  New Status: $($updatedTask.status)" -ForegroundColor Cyan
    
} catch {
    Write-Host "FAILED: Update task by ID failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 7: Token Refresh
Write-Host "Test 7: Testing token refresh..." -ForegroundColor Yellow
try {
    $refreshBody = @{
        refreshToken = $userRefreshToken
    } | ConvertTo-Json
    
    $refreshResponse = Invoke-WebRequest -Uri "$baseUrl/auth/refresh" -Method POST -Body $refreshBody -ContentType "application/json"
    $refreshData = $refreshResponse.Content | ConvertFrom-Json
    
    Write-Host "SUCCESS: Token refresh successful" -ForegroundColor Green
    Write-Host "  New Access Token generated" -ForegroundColor Cyan
    Write-Host "  New Expires At: $($refreshData.expiresAt)" -ForegroundColor Cyan
    
    $userToken = $refreshData.accessToken  # Update token for subsequent requests
    
} catch {
    Write-Host "FAILED: Token refresh failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 8: Get All Tasks After Token Refresh
Write-Host "Test 8: Getting all tasks after token refresh..." -ForegroundColor Yellow
try {
    $allTasksResponse = Invoke-WebRequest -Uri "$baseUrl/tasks" -Method GET -Headers @{Authorization="Bearer $userToken"}
    $allTasks = $allTasksResponse.Content | ConvertFrom-Json
    
    Write-Host "SUCCESS: Get all tasks successful" -ForegroundColor Green
    Write-Host "  Total tasks retrieved: $($allTasks.Count)" -ForegroundColor Cyan
    
} catch {
    Write-Host "FAILED: Get all tasks failed" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== Test Script Completed ===" -ForegroundColor Green
Write-Host ""
Write-Host "Key Points Tested:" -ForegroundColor Yellow
Write-Host "- Admin authentication and authorization" -ForegroundColor Cyan
Write-Host "- User registration (admin-only endpoint)" -ForegroundColor Cyan
Write-Host "- User authentication" -ForegroundColor Cyan
Write-Host "- Task CRUD operations with RESTful endpoints" -ForegroundColor Cyan
Write-Host "- JWT token refresh functionality" -ForegroundColor Cyan
Write-Host ""
Write-Host "Use the JWT_TodoList_API_Collection.postman_collection.json for Postman testing!" -ForegroundColor Cyan
