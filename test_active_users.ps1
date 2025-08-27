# Test script for Active Users API endpoint
Write-Host "=== Testing Active Users API Endpoint ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"

# Test 1: Login as admin to get JWT token
Write-Host "Test 1: Admin login to get JWT token..." -ForegroundColor Yellow
try {
    $loginBody = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    
    $loginHeaders = @{
        "Content-Type" = "application/json"
    }
    
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Headers $loginHeaders -Body $loginBody
    
    Write-Host "SUCCESS: Admin login successful" -ForegroundColor Green
    Write-Host "  Access Token: $($loginResponse.accessToken.Substring(0,20))..." -ForegroundColor Cyan
    
    $accessToken = $loginResponse.accessToken
    
} catch {
    Write-Host "ERROR: Admin login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: Test the new active users endpoint
Write-Host "Test 2: Testing active users endpoint..." -ForegroundColor Yellow
try {
    $activeUsersHeaders = @{
        "Authorization" = "Bearer $accessToken"
        "Content-Type" = "application/json"
    }
    
    $activeUsersResponse = Invoke-RestMethod -Uri "$baseUrl/auth/users/active" -Method GET -Headers $activeUsersHeaders
    
    Write-Host "SUCCESS: Active users endpoint successful" -ForegroundColor Green
    Write-Host "  Total Active Users: $($activeUsersResponse.totalActiveUsers)" -ForegroundColor Cyan
    Write-Host "  Success: $($activeUsersResponse.success)" -ForegroundColor Cyan
    Write-Host "  Message: $($activeUsersResponse.message)" -ForegroundColor Cyan
    Write-Host ""
    
    # Display active users for task collaboration
    Write-Host "Active Users (for task collaboration):" -ForegroundColor Yellow
    $activeUsersResponse.users | ForEach-Object {
        Write-Host "  ID: $($_.id) - Username: $($_.username)" -ForegroundColor White
    }
    
} catch {
    Write-Host "ERROR: Active users endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}
Write-Host ""

# Test 3: Try to login as a regular user and test the same endpoint
Write-Host "Test 3: Testing active users endpoint with regular user..." -ForegroundColor Yellow
try {
    # Try to login as john (regular user)
    $userLoginBody = @{
        username = "john"
        password = "password123"
    } | ConvertTo-Json
    
    try {
        $userLoginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Headers $loginHeaders -Body $userLoginBody
        
        Write-Host "SUCCESS: Regular user (john) login successful" -ForegroundColor Green
        
        $userHeaders = @{
            "Authorization" = "Bearer $($userLoginResponse.accessToken)"
            "Content-Type" = "application/json"
        }
        
        $userActiveUsersResponse = Invoke-RestMethod -Uri "$baseUrl/auth/users/active" -Method GET -Headers $userHeaders
        
        Write-Host "SUCCESS: Regular user can access active users endpoint" -ForegroundColor Green
        Write-Host "  Total Active Users: $($userActiveUsersResponse.totalActiveUsers)" -ForegroundColor Cyan
        Write-Host "  This confirms any authenticated user can access this endpoint" -ForegroundColor Cyan
        
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 401) {
            Write-Host "INFO: Regular user credentials not valid or user doesn't exist" -ForegroundColor Yellow
        } else {
            Write-Host "ERROR: Unexpected error for regular user: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "INFO: Testing with regular user - credentials may not be set up" -ForegroundColor Yellow
}
Write-Host ""

# Test 4: Test without authentication (should fail)
Write-Host "Test 4: Testing active users endpoint without authentication..." -ForegroundColor Yellow
try {
    $noAuthResponse = Invoke-RestMethod -Uri "$baseUrl/auth/users/active" -Method GET
    Write-Host "ERROR: Endpoint should require authentication!" -ForegroundColor Red
    
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Write-Host "SUCCESS: Endpoint correctly requires authentication (401 Unauthorized)" -ForegroundColor Green
    } else {
        Write-Host "INFO: Unexpected error without auth: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}
Write-Host ""

Write-Host "=== Active Users API Test Completed ===" -ForegroundColor Green
