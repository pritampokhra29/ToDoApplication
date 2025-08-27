# Test script for ToDoList API functionality
# This script tests all the issues mentioned by the user

Write-Host "=== ToDoList API Test Script ===" -ForegroundColor Green
Write-Host ""

# Test 1: Check if /auth/config endpoint is accessible (previously gave 403)
Write-Host "Test 1: Testing /auth/config endpoint..." -ForegroundColor Yellow
try {
    $configResponse = Invoke-WebRequest -Uri "http://localhost:8080/auth/config" -Method GET
    Write-Host "✓ Config endpoint accessible: $($configResponse.StatusCode)" -ForegroundColor Green
    $config = $configResponse.Content | ConvertFrom-Json
    Write-Host "  JWT Enabled: $($config.jwtEnabled)" -ForegroundColor Cyan
    Write-Host "  Auth Method: $($config.authenticationMethod)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Config endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Admin login
Write-Host "Test 2: Testing admin login..." -ForegroundColor Yellow
try {
    $loginBody = @{username="admin"; password="admin123"} | ConvertTo-Json
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Method POST -Body $loginBody -ContentType "application/json" | ConvertFrom-Json
    Write-Host "✓ Admin login successful" -ForegroundColor Green
    Write-Host "  Username: $($loginResponse.username)" -ForegroundColor Cyan
    Write-Host "  Roles: $($loginResponse.roles -join ', ')" -ForegroundColor Cyan
    $adminToken = $loginResponse.accessToken
} catch {
    Write-Host "✗ Admin login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 3: User registration with admin token (previously gave 403)
Write-Host "Test 3: Testing user registration..." -ForegroundColor Yellow
try {
    $regBody = @{username="testuser"; password="password123"; email="test@test.com"; role="USER"} | ConvertTo-Json
    $regResponse = Invoke-WebRequest -Uri "http://localhost:8080/auth/register" -Method POST -Body $regBody -ContentType "application/json" -Headers @{Authorization="Bearer $adminToken"}
    Write-Host "✓ User registration successful: $($regResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  Response: $($regResponse.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ User registration failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}
Write-Host ""

# Test 4: Login with new user
Write-Host "Test 4: Testing new user login..." -ForegroundColor Yellow
try {
    $userLoginBody = @{username="testuser"; password="password123"} | ConvertTo-Json
    $userLoginResponse = Invoke-WebRequest -Uri "http://localhost:8080/auth/login" -Method POST -Body $userLoginBody -ContentType "application/json" | ConvertFrom-Json
    Write-Host "✓ User login successful" -ForegroundColor Green
    Write-Host "  Username: $($userLoginResponse.username)" -ForegroundColor Cyan
    Write-Host "  Roles: $($userLoginResponse.roles -join ', ')" -ForegroundColor Cyan
    $userToken = $userLoginResponse.accessToken
} catch {
    Write-Host "✗ User login failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 5: Create a task
Write-Host "Test 5: Testing task creation..." -ForegroundColor Yellow
try {
    $taskBody = @{
        title="Test Task"
        description="This is a test task"
        priority="HIGH"
        status="PENDING"
        category="Work"
        dueDate="2025-08-30"
    } | ConvertTo-Json
    $taskResponse = Invoke-WebRequest -Uri "http://localhost:8080/tasks" -Method POST -Body $taskBody -ContentType "application/json" -Headers @{Authorization="Bearer $userToken"}
    Write-Host "✓ Task creation successful: $($taskResponse.StatusCode)" -ForegroundColor Green
    $task = $taskResponse.Content | ConvertFrom-Json
    Write-Host "  Task ID: $($task.id)" -ForegroundColor Cyan
    Write-Host "  Task Title: $($task.title)" -ForegroundColor Cyan
    $taskId = $task.id
} catch {
    Write-Host "✗ Task creation failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 6: Get task by ID (previously missing)
Write-Host "Test 6: Testing get task by ID..." -ForegroundColor Yellow
try {
    $getTaskResponse = Invoke-WebRequest -Uri "http://localhost:8080/tasks/$taskId" -Method GET -Headers @{Authorization="Bearer $userToken"}
    Write-Host "✓ Get task by ID successful: $($getTaskResponse.StatusCode)" -ForegroundColor Green
    $retrievedTask = $getTaskResponse.Content | ConvertFrom-Json
    Write-Host "  Retrieved Task: $($retrievedTask.title)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Get task by ID failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 7: Update task by ID (previously missing)
Write-Host "Test 7: Testing update task by ID..." -ForegroundColor Yellow
try {
    $updateBody = @{
        title="Updated Test Task"
        description="This task has been updated"
        priority="MEDIUM"
        status="IN_PROGRESS"
        category="Work"
        dueDate="2025-08-31"
    } | ConvertTo-Json
    $updateResponse = Invoke-WebRequest -Uri "http://localhost:8080/tasks/$taskId" -Method PUT -Body $updateBody -ContentType "application/json" -Headers @{Authorization="Bearer $userToken"}
    Write-Host "✓ Update task by ID successful: $($updateResponse.StatusCode)" -ForegroundColor Green
    $updatedTask = $updateResponse.Content | ConvertFrom-Json
    Write-Host "  Updated Task: $($updatedTask.title)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Update task by ID failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 8: Logout (test token invalidation)
Write-Host "Test 8: Testing logout..." -ForegroundColor Yellow
try {
    $logoutResponse = Invoke-WebRequest -Uri "http://localhost:8080/auth/logout" -Method POST -Headers @{Authorization="Bearer $userToken"}
    Write-Host "✓ Logout successful: $($logoutResponse.StatusCode)" -ForegroundColor Green
    Write-Host "  Response: $($logoutResponse.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Logout failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 9: Test that token is invalidated after logout (previously, token remained valid)
Write-Host "Test 9: Testing token invalidation after logout..." -ForegroundColor Yellow
try {
    $testResponse = Invoke-WebRequest -Uri "http://localhost:8080/tasks" -Method GET -Headers @{Authorization="Bearer $userToken"}
    Write-Host "✗ Token still valid after logout (THIS SHOULD FAIL): $($testResponse.StatusCode)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq "Unauthorized") {
        Write-Host "✓ Token properly invalidated after logout" -ForegroundColor Green
    } else {
        Write-Host "✗ Unexpected error: $($_.Exception.Message)" -ForegroundColor Red
    }
}
Write-Host ""

# Test 10: Delete task by ID (previously missing)
Write-Host "Test 10: Testing delete task by ID (with admin token)..." -ForegroundColor Yellow
try {
    $deleteResponse = Invoke-WebRequest -Uri "http://localhost:8080/tasks/$taskId" -Method DELETE -Headers @{Authorization="Bearer $adminToken"}
    Write-Host "✓ Delete task by ID successful: $($deleteResponse.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "✗ Delete task by ID failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== Test Script Completed ===" -ForegroundColor Green
