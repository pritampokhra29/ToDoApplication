# JWT Authentication Test Script for ToDoList API
# This script demonstrates the complete JWT authentication workflow

Write-Host "====== JWT Authentication Test Script ======" -ForegroundColor Green
Write-Host ""

# Test 1: Login and get JWT tokens
Write-Host "1. Testing Login..." -ForegroundColor Yellow
$loginData = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body $loginData
    Write-Host "   ✓ Login successful!" -ForegroundColor Green
    Write-Host "   Username: $($loginResponse.username)" -ForegroundColor Cyan
    Write-Host "   Roles: $($loginResponse.roles -join ', ')" -ForegroundColor Cyan
    Write-Host "   Expires: $($loginResponse.expiresAt)" -ForegroundColor Cyan
    Write-Host "   Token expires in: $($loginResponse.expiresIn) seconds" -ForegroundColor Cyan
    
    $accessToken = $loginResponse.accessToken
    $refreshToken = $loginResponse.refreshToken
} catch {
    Write-Host "   ✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 2: Access protected endpoint with JWT token
Write-Host "2. Testing Protected Endpoint Access..." -ForegroundColor Yellow
$headers = @{
    'Authorization' = "Bearer $accessToken"
}

try {
    $tasksResponse = Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Headers $headers
    Write-Host "   ✓ Protected endpoint access successful!" -ForegroundColor Green
    Write-Host "   Retrieved $($tasksResponse.Count) tasks" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Protected endpoint access failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Validate token
Write-Host "3. Testing Token Validation..." -ForegroundColor Yellow
try {
    $validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/validate" -Method POST -Headers $headers
    Write-Host "   ✓ Token validation successful!" -ForegroundColor Green
    Write-Host "   Token is valid: $($validateResponse.valid)" -ForegroundColor Cyan
    Write-Host "   Username: $($validateResponse.username)" -ForegroundColor Cyan
    Write-Host "   Remaining validity: $($validateResponse.remainingValiditySeconds) seconds" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Token validation failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 4: Check authentication status
Write-Host "4. Testing Authentication Status..." -ForegroundColor Yellow
try {
    $statusResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/status" -Method POST -Headers $headers
    Write-Host "   ✓ Status check successful!" -ForegroundColor Green
    Write-Host "   Authenticated: $($statusResponse.authenticated)" -ForegroundColor Cyan
    Write-Host "   Username: $($statusResponse.username)" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Status check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 5: Refresh token
Write-Host "5. Testing Token Refresh..." -ForegroundColor Yellow
$refreshData = @{
    refreshToken = $refreshToken
} | ConvertTo-Json

try {
    $refreshResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/refresh" -Method POST -ContentType "application/json" -Body $refreshData
    Write-Host "   ✓ Token refresh successful!" -ForegroundColor Green
    Write-Host "   New token expires: $($refreshResponse.expiresAt)" -ForegroundColor Cyan
    Write-Host "   New token expires in: $($refreshResponse.expiresIn) seconds" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Token refresh failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 6: Create a new task using JWT
Write-Host "6. Testing Task Creation with JWT..." -ForegroundColor Yellow
$newTask = @{
    title = "JWT Test Task"
    description = "Task created using JWT authentication"
    dueDate = "2025-08-30"
    status = "PENDING"
    category = "Test"
    priority = "MEDIUM"
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method POST -Headers $headers -ContentType "application/json" -Body $newTask
    Write-Host "   ✓ Task creation successful!" -ForegroundColor Green
    Write-Host "   Task ID: $($createResponse.id)" -ForegroundColor Cyan
    Write-Host "   Task Title: $($createResponse.title)" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Task creation failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 7: Test with invalid token
Write-Host "7. Testing Invalid Token Handling..." -ForegroundColor Yellow
$invalidHeaders = @{
    'Authorization' = "Bearer invalid-token-here"
}

try {
    $invalidResponse = Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Headers $invalidHeaders
    Write-Host "   ✗ Invalid token was accepted (this should not happen!)" -ForegroundColor Red
} catch {
    Write-Host "   ✓ Invalid token correctly rejected!" -ForegroundColor Green
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Cyan
}

Write-Host ""

# Test 8: Logout
Write-Host "8. Testing Logout..." -ForegroundColor Yellow
try {
    $logoutResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/logout" -Method POST -Headers $headers
    Write-Host "   ✓ Logout successful!" -ForegroundColor Green
    Write-Host "   Message: $($logoutResponse.message)" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Logout failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "====== JWT Authentication Test Completed ======" -ForegroundColor Green
Write-Host ""
Write-Host "Check the following log files for detailed audit information:" -ForegroundColor Yellow
Write-Host "   - logs/audit.log (Security and user activity events)" -ForegroundColor Cyan
Write-Host "   - logs/application.log (General application logs)" -ForegroundColor Cyan
Write-Host "   - logs/method-execution.log (Method execution details)" -ForegroundColor Cyan
Write-Host "   - logs/error.log (Error-specific logs)" -ForegroundColor Cyan
