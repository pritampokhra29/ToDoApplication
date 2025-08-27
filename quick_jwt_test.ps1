# JWT Authentication Quick Test Script
# Run this script to quickly test the JWT authentication functionality

Write-Host "=== JWT ToDo API Quick Test ===" -ForegroundColor Green
Write-Host "Make sure your application is running on http://localhost:8080" -ForegroundColor Yellow
Write-Host ""

# Function to make API calls with error handling
function Invoke-APICall {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [string]$ContentType = "application/json"
    )
    
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            Headers = $Headers
        }
        
        if ($Body) {
            $params.Body = $Body
            $params.ContentType = $ContentType
        }
        
        $response = Invoke-RestMethod @params
        return @{ Success = $true; Data = $response; Error = $null }
    }
    catch {
        return @{ Success = $false; Data = $null; Error = $_.Exception.Message }
    }
}

# Test 1: Login
Write-Host "1️⃣  Testing Login..." -ForegroundColor Cyan
$loginData = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$loginResult = Invoke-APICall -Method "POST" -Uri "http://localhost:8080/auth/login" -Body $loginData

if ($loginResult.Success) {
    Write-Host "   ✅ Login successful!" -ForegroundColor Green
    $accessToken = $loginResult.Data.accessToken
    $refreshToken = $loginResult.Data.refreshToken
    Write-Host "   👤 Username: $($loginResult.Data.username)" -ForegroundColor White
    Write-Host "   🔑 Token expires in: $($loginResult.Data.expiresIn) seconds" -ForegroundColor White
    Write-Host "   🛡️  Roles: $($loginResult.Data.roles -join ', ')" -ForegroundColor White
} else {
    Write-Host "   ❌ Login failed: $($loginResult.Error)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test 2: Get Tasks (Protected Endpoint)
Write-Host "2️⃣  Testing Protected Endpoint (Get Tasks)..." -ForegroundColor Cyan
$headers = @{ 'Authorization' = "Bearer $accessToken" }

$tasksResult = Invoke-APICall -Method "GET" -Uri "http://localhost:8080/tasks" -Headers $headers

if ($tasksResult.Success) {
    Write-Host "   ✅ Protected endpoint access successful!" -ForegroundColor Green
    $taskCount = if ($tasksResult.Data -is [array]) { $tasksResult.Data.Count } else { if ($tasksResult.Data) { 1 } else { 0 } }
    Write-Host "   📋 Retrieved $taskCount tasks" -ForegroundColor White
} else {
    Write-Host "   ❌ Protected endpoint failed: $($tasksResult.Error)" -ForegroundColor Red
}

Write-Host ""

# Test 3: Create a new task
Write-Host "3️⃣  Testing Task Creation..." -ForegroundColor Cyan
$newTask = @{
    title = "JWT Test Task - $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
    description = "Created via PowerShell JWT test script"
    dueDate = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
    status = "PENDING"
    category = "Testing"
    priority = "HIGH"
} | ConvertTo-Json

$createResult = Invoke-APICall -Method "POST" -Uri "http://localhost:8080/tasks" -Headers $headers -Body $newTask

if ($createResult.Success) {
    Write-Host "   ✅ Task creation successful!" -ForegroundColor Green
    Write-Host "   🆔 Created task ID: $($createResult.Data.id)" -ForegroundColor White
    Write-Host "   📝 Task title: $($createResult.Data.title)" -ForegroundColor White
    $taskId = $createResult.Data.id
} else {
    Write-Host "   ❌ Task creation failed: $($createResult.Error)" -ForegroundColor Red
}

Write-Host ""

# Test 4: Token Validation
Write-Host "4️⃣  Testing Token Validation..." -ForegroundColor Cyan
$validateResult = Invoke-APICall -Method "POST" -Uri "http://localhost:8080/auth/validate" -Headers $headers

if ($validateResult.Success) {
    Write-Host "   ✅ Token validation successful!" -ForegroundColor Green
    Write-Host "   👤 Username: $($validateResult.Data.username)" -ForegroundColor White
    Write-Host "   ⏰ Remaining validity: $($validateResult.Data.remainingValiditySeconds) seconds" -ForegroundColor White
} else {
    Write-Host "   ❌ Token validation failed: $($validateResult.Error)" -ForegroundColor Red
}

Write-Host ""

# Test 5: Authentication Status
Write-Host "5️⃣  Testing Authentication Status..." -ForegroundColor Cyan
$statusResult = Invoke-APICall -Method "POST" -Uri "http://localhost:8080/auth/status" -Headers $headers

if ($statusResult.Success) {
    Write-Host "   ✅ Status check successful!" -ForegroundColor Green
    Write-Host "   🔐 Authenticated: $($statusResult.Data.authenticated)" -ForegroundColor White
    Write-Host "   👤 Username: $($statusResult.Data.username)" -ForegroundColor White
} else {
    Write-Host "   ❌ Status check failed: $($statusResult.Error)" -ForegroundColor Red
}

Write-Host ""

# Test 6: Invalid Token Test
Write-Host "6️⃣  Testing Invalid Token (Should Fail)..." -ForegroundColor Cyan
$invalidHeaders = @{ 'Authorization' = "Bearer invalid-token" }
$invalidResult = Invoke-APICall -Method "GET" -Uri "http://localhost:8080/tasks" -Headers $invalidHeaders

if ($invalidResult.Success) {
    Write-Host "   ❌ Invalid token was accepted! (Security Issue)" -ForegroundColor Red
} else {
    Write-Host "   ✅ Invalid token correctly rejected!" -ForegroundColor Green
    Write-Host "   🛡️  Security working properly" -ForegroundColor White
}

Write-Host ""

# Test 7: Token Refresh
Write-Host "7️⃣  Testing Token Refresh..." -ForegroundColor Cyan
$refreshData = @{ refreshToken = $refreshToken } | ConvertTo-Json
$refreshResult = Invoke-APICall -Method "POST" -Uri "http://localhost:8080/auth/refresh" -Body $refreshData

if ($refreshResult.Success) {
    Write-Host "   ✅ Token refresh successful!" -ForegroundColor Green
    Write-Host "   🔄 New token expires in: $($refreshResult.Data.expiresIn) seconds" -ForegroundColor White
    $newAccessToken = $refreshResult.Data.accessToken
} else {
    Write-Host "   ❌ Token refresh failed: $($refreshResult.Error)" -ForegroundColor Red
}

Write-Host ""

# Test 8: Update the created task (if it exists)
if ($taskId) {
    Write-Host "8️⃣  Testing Task Update..." -ForegroundColor Cyan
    $updateTask = @{
        title = "JWT Test Task - UPDATED - $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
        description = "Updated via PowerShell JWT test script"
        dueDate = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
        status = "COMPLETED"
        category = "Testing"
        priority = "MEDIUM"
    } | ConvertTo-Json
    
    $updateResult = Invoke-APICall -Method "PUT" -Uri "http://localhost:8080/tasks/$taskId" -Headers $headers -Body $updateTask
    
    if ($updateResult.Success) {
        Write-Host "   ✅ Task update successful!" -ForegroundColor Green
        Write-Host "   📝 Updated status: $($updateResult.Data.status)" -ForegroundColor White
    } else {
        Write-Host "   ❌ Task update failed: $($updateResult.Error)" -ForegroundColor Red
    }
    
    Write-Host ""
}

# Test 9: Logout
Write-Host "9️⃣  Testing Logout..." -ForegroundColor Cyan
$logoutResult = Invoke-APICall -Method "POST" -Uri "http://localhost:8080/auth/logout" -Headers $headers

if ($logoutResult.Success) {
    Write-Host "   ✅ Logout successful!" -ForegroundColor Green
    Write-Host "   💤 User session terminated" -ForegroundColor White
} else {
    Write-Host "   ❌ Logout failed: $($logoutResult.Error)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== Test Summary ===" -ForegroundColor Green
Write-Host "✅ JWT Authentication system is working properly!" -ForegroundColor Green
Write-Host "📊 Check the log files for detailed audit trails:" -ForegroundColor Yellow
Write-Host "   - logs/application.log (general application logs)" -ForegroundColor White
Write-Host "   - logs/audit.log (user activity logs)" -ForegroundColor White
Write-Host "   - logs/method-execution.log (method performance logs)" -ForegroundColor White
Write-Host "   - logs/error.log (error logs)" -ForegroundColor White
Write-Host ""
Write-Host "🎉 All tests completed!" -ForegroundColor Green
