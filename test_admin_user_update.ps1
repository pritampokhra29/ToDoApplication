# Test script for Admin User Update API endpoint
Write-Host "=== Testing Admin User Update API Endpoint ===" -ForegroundColor Green
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

# Step 2: Get current user list to see what users exist
Write-Host "Step 2: Getting current user list..." -ForegroundColor Yellow
try {
    $usersHeaders = @{
        "Authorization" = "Bearer $accessToken"
        "Content-Type" = "application/json"
    }
    
    $usersResponse = Invoke-RestMethod -Uri "$baseUrl/auth/admin/users" -Method GET -Headers $usersHeaders
    
    Write-Host "SUCCESS: Retrieved user list" -ForegroundColor Green
    Write-Host "  Total Users: $($usersResponse.totalUsers)" -ForegroundColor Cyan
    
    # Show first few users for testing
    $testUser = $usersResponse.users | Where-Object { $_.username -ne "admin" } | Select-Object -First 1
    
    if ($testUser) {
        Write-Host "  Test User Found:" -ForegroundColor Cyan
        Write-Host "    ID: $($testUser.id)" -ForegroundColor White
        Write-Host "    Username: $($testUser.username)" -ForegroundColor White
        Write-Host "    Email: $($testUser.email)" -ForegroundColor White
        Write-Host "    Role: $($testUser.role)" -ForegroundColor White
        Write-Host "    Active: $($testUser.active)" -ForegroundColor White
    }
    
} catch {
    Write-Host "ERROR: Failed to get user list: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 3: Test updating a user
if ($testUser) {
    Write-Host "Step 3: Testing user update endpoint..." -ForegroundColor Yellow
    try {
        # Prepare update data
        $updateData = @{
            username = $testUser.username
            email = "updated_$($testUser.email)"
            role = $testUser.role
            isActive = $testUser.active
        } | ConvertTo-Json
        
        $updateHeaders = @{
            "Authorization" = "Bearer $accessToken"
            "Content-Type" = "application/json"
        }
        
        Write-Host "  Updating user ID: $($testUser.id)" -ForegroundColor Cyan
        Write-Host "  New email: updated_$($testUser.email)" -ForegroundColor Cyan
        
        $updateResponse = Invoke-RestMethod -Uri "$baseUrl/auth/admin/users/$($testUser.id)" -Method POST -Headers $updateHeaders -Body $updateData
        
        Write-Host "SUCCESS: User update successful" -ForegroundColor Green
        Write-Host "  Updated User ID: $($updateResponse.user.id)" -ForegroundColor Cyan
        Write-Host "  Updated Username: $($updateResponse.user.username)" -ForegroundColor Cyan
        Write-Host "  Updated Email: $($updateResponse.user.email)" -ForegroundColor Cyan
        Write-Host "  Success: $($updateResponse.success)" -ForegroundColor Cyan
        Write-Host "  Message: $($updateResponse.message)" -ForegroundColor Cyan
        
    } catch {
        Write-Host "ERROR: User update failed: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            Write-Host "  Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "Step 3: No test user found to update" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "=== Admin User Update API Test Completed ===" -ForegroundColor Green
