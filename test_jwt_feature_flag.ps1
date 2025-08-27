# JWT Feature Flag Demo Script
# This script demonstrates how to use the JWT feature flag

Write-Host "=== JWT Feature Flag Demo ===" -ForegroundColor Green
Write-Host "This script demonstrates switching between JWT and Basic Authentication" -ForegroundColor Yellow
Write-Host ""

# Function to check authentication configuration
function Test-AuthConfig {
    try {
        $config = Invoke-RestMethod -Uri "http://localhost:8080/auth/config" -Method GET
        Write-Host "🔧 Current Configuration:" -ForegroundColor Cyan
        Write-Host "   Authentication Method: $($config.authenticationMethod)" -ForegroundColor White
        Write-Host "   JWT Enabled: $($config.jwtEnabled)" -ForegroundColor White
        Write-Host "   Message: $($config.message)" -ForegroundColor White
        return $config.jwtEnabled
    }
    catch {
        Write-Host "❌ Could not check configuration. Is the application running?" -ForegroundColor Red
        return $null
    }
}

# Function to test JWT authentication
function Test-JWTAuth {
    Write-Host "`n🔐 Testing JWT Authentication..." -ForegroundColor Cyan
    
    # Try to login
    $loginData = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    
    try {
        $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body $loginData
        Write-Host "   ✅ JWT Login successful!" -ForegroundColor Green
        Write-Host "   🎫 Token received (expires in $($loginResponse.expiresIn) seconds)" -ForegroundColor White
        
        # Test protected endpoint with JWT token
        $headers = @{ 'Authorization' = "Bearer $($loginResponse.accessToken)" }
        $tasks = Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Headers $headers
        $taskCount = if ($tasks -is [array]) { $tasks.Count } else { if ($tasks) { 1 } else { 0 } }
        Write-Host "   ✅ Protected endpoint access successful (retrieved $taskCount tasks)" -ForegroundColor Green
        
        return $true
    }
    catch {
        Write-Host "   ❌ JWT Authentication failed: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to test Basic authentication
function Test-BasicAuth {
    Write-Host "`n🔑 Testing Basic Authentication..." -ForegroundColor Cyan
    
    try {
        # Create Basic Auth header
        $credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
        $headers = @{ 'Authorization' = "Basic $credentials" }
        
        # Test protected endpoint with Basic Auth
        $tasks = Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Headers $headers
        $taskCount = if ($tasks -is [array]) { $tasks.Count } else { if ($tasks) { 1 } else { 0 } }
        Write-Host "   ✅ Basic Authentication successful (retrieved $taskCount tasks)" -ForegroundColor Green
        
        return $true
    }
    catch {
        Write-Host "   ❌ Basic Authentication failed: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to test JWT endpoints when JWT is disabled
function Test-JWTEndpointsWhenDisabled {
    Write-Host "`n🚫 Testing JWT Endpoints When JWT is Disabled..." -ForegroundColor Cyan
    
    $loginData = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body $loginData
        Write-Host "   ❌ JWT endpoint should be disabled but returned: $($response)" -ForegroundColor Red
        return $false
    }
    catch {
        if ($_.Exception.Response.StatusCode -eq 405) {
            Write-Host "   ✅ JWT login correctly disabled (405 Method Not Allowed)" -ForegroundColor Green
            return $true
        }
        else {
            Write-Host "   ⚠️  Unexpected error: $($_.Exception.Message)" -ForegroundColor Yellow
            return $false
        }
    }
}

# Main test execution
Write-Host "1️⃣  Checking current authentication configuration..." -ForegroundColor Yellow
$isJwtEnabled = Test-AuthConfig

if ($isJwtEnabled -eq $null) {
    Write-Host "`n❌ Cannot continue. Please start the application first." -ForegroundColor Red
    exit 1
}

if ($isJwtEnabled) {
    Write-Host "`n2️⃣  JWT is ENABLED - Testing JWT Authentication Flow..." -ForegroundColor Yellow
    $jwtSuccess = Test-JWTAuth
    
    Write-Host "`n3️⃣  Testing Basic Auth (should fail when JWT is enabled)..." -ForegroundColor Yellow
    $basicSuccess = Test-BasicAuth
    
    if ($jwtSuccess -and !$basicSuccess) {
        Write-Host "`n✅ JWT Mode is working correctly!" -ForegroundColor Green
        Write-Host "   - JWT authentication works" -ForegroundColor White
        Write-Host "   - Basic auth is rejected (as expected)" -ForegroundColor White
    }
}
else {
    Write-Host "`n2️⃣  JWT is DISABLED - Testing Basic Authentication Flow..." -ForegroundColor Yellow
    $basicSuccess = Test-BasicAuth
    
    Write-Host "`n3️⃣  Testing JWT endpoints (should be disabled)..." -ForegroundColor Yellow
    $jwtDisabled = Test-JWTEndpointsWhenDisabled
    
    if ($basicSuccess -and $jwtDisabled) {
        Write-Host "`n✅ Basic Auth Mode is working correctly!" -ForegroundColor Green
        Write-Host "   - Basic authentication works" -ForegroundColor White
        Write-Host "   - JWT endpoints are disabled (as expected)" -ForegroundColor White
    }
}

Write-Host "`n=== Demo Instructions ===" -ForegroundColor Green
Write-Host "To switch authentication modes:" -ForegroundColor Yellow
Write-Host "1. Edit application.properties" -ForegroundColor White
Write-Host "2. Change jwt.enabled=true (for JWT) or jwt.enabled=false (for Basic Auth)" -ForegroundColor White
Write-Host "3. Restart the application" -ForegroundColor White
Write-Host "4. Run this script again to test the new mode" -ForegroundColor White
Write-Host ""
Write-Host "📚 See JWT_FEATURE_FLAG_GUIDE.md for complete documentation" -ForegroundColor Cyan
