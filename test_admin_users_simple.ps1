# Test script for Admin Users API endpoint
Write-Host "=== Testing Admin Users API Endpoint ===" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"

# Step 1: Login as admin to get JWT token
Write-Host "Step 1: Admin login to get JWT token..." -ForegroundColor Yellow
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

# Step 2: Test the new admin users endpoint
Write-Host "Step 2: Testing admin users endpoint..." -ForegroundColor Yellow
try {
    $usersHeaders = @{
        "Authorization" = "Bearer $accessToken"
        "Content-Type" = "application/json"
    }
    
    $usersResponse = Invoke-RestMethod -Uri "$baseUrl/auth/admin/users" -Method GET -Headers $usersHeaders
    
    Write-Host "SUCCESS: Admin users endpoint successful" -ForegroundColor Green
    Write-Host "  Total Users: $($usersResponse.totalUsers)" -ForegroundColor Cyan
    Write-Host "  Success: $($usersResponse.success)" -ForegroundColor Cyan
    Write-Host "  Message: $($usersResponse.message)" -ForegroundColor Cyan
    Write-Host ""
    
    # Display users information
    Write-Host "Users List:" -ForegroundColor Yellow
    $usersResponse.users | ForEach-Object {
        Write-Host "  ID: $($_.id)" -ForegroundColor White
        Write-Host "  Username: $($_.username)" -ForegroundColor White
        Write-Host "  Email: $($_.email)" -ForegroundColor White
        Write-Host "  Role: $($_.role)" -ForegroundColor White
        Write-Host "  Active: $($_.active)" -ForegroundColor White
        Write-Host "  Created: $($_.createdAt)" -ForegroundColor White
        Write-Host "  ---" -ForegroundColor Gray
    }
    
} catch {
    Write-Host "ERROR: Admin users endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "=== Admin Users API Test Completed ===" -ForegroundColor Green
