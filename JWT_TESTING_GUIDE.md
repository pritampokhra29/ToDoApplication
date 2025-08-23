# 🧪 JWT Authentication Testing Guide

## 📋 **Step-by-Step Testing Instructions**

### **Prerequisites**
1. ✅ Application is running on `http://localhost:8080`
2. ✅ JWT authentication is enabled (`jwt.enabled=true` in application.properties)
3. ✅ Default users are available in the database

> **💡 Note:** This application supports a JWT feature flag. If `jwt.enabled=false`, the application uses Basic Authentication instead. See `JWT_FEATURE_FLAG_GUIDE.md` for details.

### **Check Authentication Mode**
Before testing, verify the current authentication mode:
```bash
GET /auth/config
```

**Expected Response (JWT Enabled):**
```json
{
    "jwtEnabled": true,
    "authenticationMethod": "JWT",
    "message": "JWT Authentication is enabled"
}
```

### **Available Test Users**
```
Username: admin    | Password: admin123    | Role: ADMIN
Username: john     | Password: password123 | Role: USER  
Username: jane     | Password: password123 | Role: USER
Username: mike     | Password: password123 | Role: USER
Username: sarah    | Password: password123 | Role: USER
```

---

## 🔐 **Test Scenario 1: Complete Authentication Flow**

### **Step 1: Login and Get JWT Tokens**
**Endpoint:** `POST /auth/login`

**Request:**
```json
{
    "username": "admin",
    "password": "admin123"
}
```

**Expected Response:**
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "username": "admin",
    "roles": ["ROLE_ADMIN"],
    "expiresIn": 86400,
    "issuedAt": "2025-08-23 15:30:45",
    "expiresAt": "2025-08-24 15:30:45"
}
```

**⚠️ Important:** Copy the `accessToken` for subsequent requests!

---

### **Step 2: Access Protected Endpoint**
**Endpoint:** `GET /tasks`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: application/json
```

**Expected Response:** List of tasks for the authenticated user

---

### **Step 3: Validate Token**
**Endpoint:** `POST /auth/validate`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response:**
```json
{
    "valid": true,
    "username": "admin",
    "remainingValiditySeconds": 86350
}
```

---

### **Step 4: Check Authentication Status**
**Endpoint:** `POST /auth/status`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response:**
```json
{
    "authenticated": true,
    "username": "admin",
    "authorities": [
        {
            "authority": "ROLE_ADMIN"
        }
    ]
}
```

---

### **Step 5: Refresh Access Token**
**Endpoint:** `POST /auth/refresh`

**Request:**
```json
{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
}
```

**Expected Response:** New JWT tokens with updated expiration

---

### **Step 6: Create New Task (Testing CRUD)**
**Endpoint:** `POST /tasks`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
Content-Type: application/json
```

**Request:**
```json
{
    "title": "JWT Authentication Test Task",
    "description": "Testing JWT authentication with new task creation",
    "dueDate": "2025-08-30",
    "status": "PENDING",
    "category": "Testing",
    "priority": "HIGH"
}
```

---

### **Step 7: Test Invalid Token**
**Endpoint:** `GET /tasks`

**Headers:**
```
Authorization: Bearer invalid-token-here
```

**Expected Response:** `401 Unauthorized` with error message

---

### **Step 8: Logout**
**Endpoint:** `POST /auth/logout`

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
```

**Expected Response:**
```json
{
    "message": "Logout successful"
}
```

---

## 🧪 **Test Scenario 2: Error Handling**

### **Test Invalid Credentials**
**Endpoint:** `POST /auth/login`

**Request:**
```json
{
    "username": "admin",
    "password": "wrongpassword"
}
```

**Expected Response:** `401 Unauthorized`

### **Test Expired Token**
Use an expired token to access protected endpoints

### **Test Malformed Token**
Use a malformed token (e.g., "Bearer invalid.token.here")

---

## 🔧 **Test Scenario 3: Role-Based Access**

### **Test Admin Registration (Should Work)**
**Endpoint:** `POST /auth/register`

**Headers:** (Use admin token)
```
Authorization: Bearer ADMIN_ACCESS_TOKEN
Content-Type: application/json
```

**Request:**
```json
{
    "username": "newuser",
    "password": "password123",
    "email": "newuser@example.com",
    "role": "USER"
}
```

### **Test User Registration (Should Fail)**
**Endpoint:** `POST /auth/register`

**Headers:** (Use regular user token)
```
Authorization: Bearer USER_ACCESS_TOKEN
Content-Type: application/json
```

**Expected Response:** `403 Forbidden`

---

## 📊 **PowerShell Testing Script**

Save this as `jwt-test.ps1`:

```powershell
# JWT Authentication Complete Test Script

Write-Host "=== JWT Authentication Test Suite ===" -ForegroundColor Green

# Test 1: Login
Write-Host "`n1. Testing Login..." -ForegroundColor Yellow
$loginData = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body $loginData
    Write-Host "   ✓ Login successful!" -ForegroundColor Green
    $accessToken = $loginResponse.accessToken
    $refreshToken = $loginResponse.refreshToken
    Write-Host "   Token expires in: $($loginResponse.expiresIn) seconds" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Protected Endpoint
Write-Host "`n2. Testing Protected Endpoint..." -ForegroundColor Yellow
$headers = @{ 'Authorization' = "Bearer $accessToken" }

try {
    $tasksResponse = Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Headers $headers
    Write-Host "   ✓ Protected endpoint access successful!" -ForegroundColor Green
    Write-Host "   Retrieved $($tasksResponse.Count) tasks" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Protected endpoint failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Token Validation
Write-Host "`n3. Testing Token Validation..." -ForegroundColor Yellow
try {
    $validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/validate" -Method POST -Headers $headers
    Write-Host "   ✓ Token validation successful!" -ForegroundColor Green
    Write-Host "   Username: $($validateResponse.username)" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Token validation failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Create Task
Write-Host "`n4. Testing Task Creation..." -ForegroundColor Yellow
$newTask = @{
    title = "JWT Test Task"
    description = "Created via JWT authentication test"
    dueDate = "2025-08-30"
    status = "PENDING"
    category = "Testing"
    priority = "MEDIUM"
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method POST -Headers $headers -ContentType "application/json" -Body $newTask
    Write-Host "   ✓ Task creation successful!" -ForegroundColor Green
    Write-Host "   Created task ID: $($createResponse.id)" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Task creation failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Token Refresh
Write-Host "`n5. Testing Token Refresh..." -ForegroundColor Yellow
$refreshData = @{ refreshToken = $refreshToken } | ConvertTo-Json

try {
    $refreshResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/refresh" -Method POST -ContentType "application/json" -Body $refreshData
    Write-Host "   ✓ Token refresh successful!" -ForegroundColor Green
    Write-Host "   New token expires in: $($refreshResponse.expiresIn) seconds" -ForegroundColor Cyan
} catch {
    Write-Host "   ✗ Token refresh failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Invalid Token
Write-Host "`n6. Testing Invalid Token..." -ForegroundColor Yellow
$invalidHeaders = @{ 'Authorization' = "Bearer invalid-token" }

try {
    $invalidResponse = Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Headers $invalidHeaders
    Write-Host "   ✗ Invalid token was accepted!" -ForegroundColor Red
} catch {
    Write-Host "   ✓ Invalid token correctly rejected!" -ForegroundColor Green
}

Write-Host "`n=== Test Suite Complete ===" -ForegroundColor Green
```

To run: `powershell -ExecutionPolicy Bypass -File jwt-test.ps1`

---

## 📊 **Monitoring Test Results**

### **Check Log Files**
```bash
# View audit logs
Get-Content ".\logs\audit.log" -Tail 20

# View application logs  
Get-Content ".\logs\application.log" -Tail 20

# View method execution logs
Get-Content ".\logs\method-execution.log" -Tail 20
```

### **Expected Log Entries**
- Authentication attempts and results
- Token generation and validation
- Method execution times
- Security events
- User activities

---

## 🐛 **Troubleshooting**

### **Common Issues**

**1. 401 Unauthorized**
- Check username/password combination
- Verify token format: `Bearer TOKEN`
- Ensure token hasn't expired

**2. 403 Forbidden**
- Check user role permissions
- Verify endpoint authorization requirements

**3. Token Validation Fails**
- Check token format and encoding
- Verify JWT secret configuration
- Check token expiration time

**4. Connection Refused**
- Ensure application is running on port 8080
- Check firewall settings
- Verify application startup logs

### **Debug Commands**
```bash
# Check application status
curl http://localhost:8080/actuator/health

# Check JWT configuration
grep "jwt" src/main/resources/application.properties

# View recent logs
tail -f logs/application.log
```

This comprehensive testing guide will help you validate all aspects of the JWT authentication implementation!
