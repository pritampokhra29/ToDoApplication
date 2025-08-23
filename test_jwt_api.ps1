# Comprehensive API Test Script for JWT TodoList API
# Tests all endpoints including authentication, task CRUD, and token management

Write-Host "=== JWT TodoList API Comprehensive Test Script ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"

# Test 1: Check Authentication Configuration
Write-Host "Test 1: Checking authentication configuration..." -ForegroundColor Yellow
try {
    $configResponse = Invoke-WebRequest -Uri "$baseUrl/auth/config" -Method GET
    Write-Host "✓ Config endpoint accessible: $($configResponse.StatusCode)" -ForegroundColor Green
    $config = $configResponse.Content | ConvertFrom-Json
    Write-Host "  JWT Enabled: $($config.jwtEnabled)" -ForegroundColor Cyan
    Write-Host "  Auth Method: $($config.authenticationMethod)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Config endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: Admin Login
Write-Host "Test 2: Admin login to get JWT tokens..." -ForegroundColor Yellow
try {
    $loginBody = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $loginData = $loginResponse.Content | ConvertFrom-Json
    
    Write-Host "✓ Admin login successful" -ForegroundColor Green
    Write-Host "  Username: $($loginData.username)" -ForegroundColor Cyan
    Write-Host "  Roles: $($loginData.roles -join ', ')" -ForegroundColor Cyan
    Write-Host "  Token Type: $($loginData.tokenType)" -ForegroundColor Cyan
    Write-Host "  Expires At: $($loginData.expiresAt)" -ForegroundColor Cyan
    
    $adminToken = $loginData.accessToken
    $adminRefreshToken = $loginData.refreshToken
    
} catch {
    Write-Host "✗ Admin login failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  Response: $($_.Exception.Response)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 3: User Registration (Admin Required)
Write-Host "Test 3: Testing user registration with admin token..." -ForegroundColor Yellow
try {
    $regBody = @{
        username = "testuser_$(Get-Random)"
        password = "password123"
        email = "testuser@example.com"
        role = "USER"
    } | ConvertTo-Json
    
    $regResponse = Invoke-WebRequest -Uri "$baseUrl/auth/register" -Method POST -Body $regBody -ContentType "application/json" -Headers @{Authorization="Bearer $adminToken"}
    Write-Host "✓ User registration successful: $($regResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  Response: $($regResponse.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ User registration failed: $($_.Exception.Message)" -ForegroundColor Red
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

# Test 4: Regular User Login
Write-Host "Test 4: Regular user login..." -ForegroundColor Yellow
try {
    $userLoginBody = @{
        username = "john"
        password = "password123"
    } | ConvertTo-Json
    
    $userLoginResponse = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method POST -Body $userLoginBody -ContentType "application/json"
    $userLoginData = $userLoginResponse.Content | ConvertFrom-Json
    
    Write-Host "✓ User login successful" -ForegroundColor Green
    Write-Host "  Username: $($userLoginData.username)" -ForegroundColor Cyan
    Write-Host "  Roles: $($userLoginData.roles -join ', ')" -ForegroundColor Cyan
    
    $userToken = $userLoginData.accessToken
    $userRefreshToken = $userLoginData.refreshToken
    
} catch {
    Write-Host "✗ User login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 5: Create Task
Write-Host "Test 5: Creating a new task..." -ForegroundColor Yellow
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
    
    Write-Host "✓ Task creation successful: $($taskResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  Task ID: $($task.id)" -ForegroundColor Cyan
    Write-Host "  Task Title: $($task.title)" -ForegroundColor Cyan
    
    $taskId = $task.id
    
} catch {
    Write-Host "✗ Task creation failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 6: Get Task by ID (RESTful endpoint)
Write-Host "Test 6: Getting task by ID using RESTful endpoint..." -ForegroundColor Yellow
try {
    $getTaskResponse = Invoke-WebRequest -Uri "$baseUrl/tasks/$taskId" -Method GET -Headers @{Authorization="Bearer $userToken"}
    $retrievedTask = $getTaskResponse.Content | ConvertFrom-Json
    
    Write-Host "✓ Get task by ID successful: $($getTaskResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  Retrieved Task: $($retrievedTask.title)" -ForegroundColor Cyan
    Write-Host "  Task Status: $($retrievedTask.status)" -ForegroundColor Cyan
    
} catch {
    Write-Host "✗ Get task by ID failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 7: Update Task by ID (RESTful endpoint)
Write-Host "Test 7: Updating task by ID using RESTful endpoint..." -ForegroundColor Yellow
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
    
    Write-Host "✓ Update task by ID successful: $($updateResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  Updated Task: $($updatedTask.title)" -ForegroundColor Cyan
    Write-Host "  New Status: $($updatedTask.status)" -ForegroundColor Cyan
    
} catch {
    Write-Host "✗ Update task by ID failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 8: Token Refresh
Write-Host "Test 8: Testing token refresh..." -ForegroundColor Yellow
try {
    $refreshBody = @{
        refreshToken = $userRefreshToken
    } | ConvertTo-Json
    
    $refreshResponse = Invoke-WebRequest -Uri "$baseUrl/auth/refresh" -Method POST -Body $refreshBody -ContentType "application/json"
    $refreshData = $refreshResponse.Content | ConvertFrom-Json
    
    Write-Host "✓ Token refresh successful: $($refreshResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  New Access Token generated" -ForegroundColor Cyan
    Write-Host "  New Expires At: $($refreshData.expiresAt)" -ForegroundColor Cyan
    
    $userToken = $refreshData.accessToken  # Update token for subsequent requests
    
} catch {
    Write-Host "✗ Token refresh failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 9: Get All Tasks
Write-Host "Test 9: Getting all tasks..." -ForegroundColor Yellow
try {
    $allTasksResponse = Invoke-WebRequest -Uri "$baseUrl/tasks" -Method GET -Headers @{Authorization="Bearer $userToken"}
    $allTasks = $allTasksResponse.Content | ConvertFrom-Json
    
    Write-Host "✓ Get all tasks successful: $($allTasksResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  Total tasks retrieved: $($allTasks.Count)" -ForegroundColor Cyan
    
} catch {
    Write-Host "✗ Get all tasks failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 10: Logout with token invalidation
Write-Host "Test 10: Testing logout with token invalidation..." -ForegroundColor Yellow
try {
    $logoutBody = @{
        refreshToken = $userRefreshToken
    } | ConvertTo-Json
    
    $logoutResponse = Invoke-WebRequest -Uri "$baseUrl/auth/logout" -Method POST -Body $logoutBody -ContentType "application/json" -Headers @{Authorization="Bearer $userToken"}
    
    Write-Host "✓ Logout successful: $($logoutResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  Response: $($logoutResponse.Content)" -ForegroundColor Cyan
    
} catch {
    Write-Host "✗ Logout failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 11: Verify token invalidation
Write-Host "Test 11: Verifying token invalidation after logout..." -ForegroundColor Yellow
try {
    $testResponse = Invoke-WebRequest -Uri "$baseUrl/tasks" -Method GET -Headers @{Authorization="Bearer $userToken"}
    Write-Host "✗ Token still valid after logout (THIS SHOULD FAIL): $($testResponse.StatusCode)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "✓ Token properly invalidated after logout (401 Unauthorized)" -ForegroundColor Green
    } else {
        Write-Host "✗ Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
    }
}
Write-Host ""

# Test 12: Delete Task by ID (RESTful endpoint) - using admin token
Write-Host "Test 12: Deleting task by ID using admin token..." -ForegroundColor Yellow
try {
    $deleteResponse = Invoke-WebRequest -Uri "$baseUrl/tasks/$taskId" -Method DELETE -Headers @{Authorization="Bearer $adminToken"}
    
    Write-Host "✓ Delete task by ID successful: $($deleteResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  Task deleted successfully" -ForegroundColor Cyan
    
} catch {
    Write-Host "✗ Delete task by ID failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== JWT TodoList API Test Script Completed ===" -ForegroundColor Green
Write-Host ""
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "- Authentication endpoints tested ✓" -ForegroundColor Green
Write-Host "- JWT token management tested ✓" -ForegroundColor Green  
Write-Host "- RESTful task CRUD endpoints tested ✓" -ForegroundColor Green
Write-Host "- Token refresh functionality tested ✓" -ForegroundColor Green
Write-Host "- Token invalidation on logout tested ✓" -ForegroundColor Green
Write-Host ""
Write-Host "Use the JWT_TodoList_API_Collection.postman_collection.json file in Postman for GUI testing!" -ForegroundColor Cyan
