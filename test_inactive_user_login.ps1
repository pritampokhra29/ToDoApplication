# Test script to verify inactive user login behavior
Write-Host "=== Testing Inactive User Login Prevention ===" -ForegroundColor Green
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

# Step 2: Find a test user and verify they can login initially
Write-Host "Step 2: Testing initial login with john..." -ForegroundColor Yellow
try {
    $userLoginBody = @{
        username = "john"
        password = "password123"
    } | ConvertTo-Json
    
    $userLoginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Headers $loginHeaders -Body $userLoginBody
    
    Write-Host "SUCCESS: john can login initially" -ForegroundColor Green
    Write-Host "  john's Access Token: $($userLoginResponse.accessToken.Substring(0,20))..." -ForegroundColor Cyan
    
} catch {
    Write-Host "ERROR: john cannot login initially: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "This might mean john's credentials are not set up correctly" -ForegroundColor Yellow
}
Write-Host ""

# Step 3: Deactivate john using admin endpoint
Write-Host "Step 3: Deactivating john's account..." -ForegroundColor Yellow
try {
    $updateHeaders = @{
        "Authorization" = "Bearer $accessToken"
        "Content-Type" = "application/json"
    }
    
    # First get john's current details
    $usersResponse = Invoke-RestMethod -Uri "$baseUrl/auth/admin/users" -Method GET -Headers $updateHeaders
    $johnUser = $usersResponse.users | Where-Object { $_.username -eq "john" }
    
    if ($johnUser) {
        Write-Host "  Found john's account - ID: $($johnUser.id)" -ForegroundColor Cyan
        
        # Update john to be inactive
        $deactivateData = @{
            username = $johnUser.username
            email = $johnUser.email
            role = $johnUser.role
            isActive = $false  # Deactivate the user
        } | ConvertTo-Json
        
        $updateResponse = Invoke-RestMethod -Uri "$baseUrl/auth/admin/users/$($johnUser.id)" -Method POST -Headers $updateHeaders -Body $deactivateData
        
        Write-Host "SUCCESS: john's account deactivated" -ForegroundColor Green
        Write-Host "  Active status: $($updateResponse.user.active)" -ForegroundColor Cyan
        
    } else {
        Write-Host "ERROR: Could not find john's account" -ForegroundColor Red
        exit 1
    }
    
} catch {
    Write-Host "ERROR: Failed to deactivate john: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 4: Try to login with deactivated john
Write-Host "Step 4: Testing login with deactivated john..." -ForegroundColor Yellow
try {
    $userLoginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Headers $loginHeaders -Body $userLoginBody
    
    Write-Host "ERROR: Deactivated user should NOT be able to login!" -ForegroundColor Red
    Write-Host "  This indicates a security issue - inactive users can still login" -ForegroundColor Red
    
} catch {
    if ($_.Exception.Message -like "*401*" -or $_.Exception.Message -like "*Unauthorized*" -or $_.Exception.Message -like "*invalid*") {
        Write-Host "SUCCESS: Deactivated user correctly denied login" -ForegroundColor Green
        Write-Host "  Error message: $($_.Exception.Message)" -ForegroundColor Cyan
    } else {
        Write-Host "INFO: Unexpected error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}
Write-Host ""

# Step 5: Reactivate john for cleanup
Write-Host "Step 5: Reactivating john's account for cleanup..." -ForegroundColor Yellow
try {
    $reactivateData = @{
        username = $johnUser.username
        email = $johnUser.email
        role = $johnUser.role
        isActive = $true  # Reactivate the user
    } | ConvertTo-Json
    
    $reactivateResponse = Invoke-RestMethod -Uri "$baseUrl/auth/admin/users/$($johnUser.id)" -Method POST -Headers $updateHeaders -Body $reactivateData
    
    Write-Host "SUCCESS: john's account reactivated for cleanup" -ForegroundColor Green
    Write-Host "  Active status: $($reactivateResponse.user.active)" -ForegroundColor Cyan
    
} catch {
    Write-Host "ERROR: Failed to reactivate john: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Step 6: Verify john can login again after reactivation
Write-Host "Step 6: Verifying john can login after reactivation..." -ForegroundColor Yellow
try {
    $finalLoginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Headers $loginHeaders -Body $userLoginBody
    
    Write-Host "SUCCESS: john can login again after reactivation" -ForegroundColor Green
    Write-Host "  john's Access Token: $($finalLoginResponse.accessToken.Substring(0,20))..." -ForegroundColor Cyan
    
} catch {
    Write-Host "INFO: john cannot login after reactivation: $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "=== Inactive User Login Test Completed ===" -ForegroundColor Green
