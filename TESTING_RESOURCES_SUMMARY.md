# 🧪 JWT Authentication Testing Resources

## 📁 **What You Have**

I've created comprehensive testing resources for your JWT authentication system:

### **1. 📖 Testing Guide** - `JWT_TESTING_GUIDE.md`
- **Complete step-by-step instructions** for testing all JWT endpoints
- **Sample requests and expected responses** for each endpoint
- **PowerShell testing commands** for quick verification
- **Troubleshooting guide** for common issues
- **Log monitoring instructions** for audit trails

### **2. 🔧 Postman Collection** - `JWT_TodoList_API_Complete.postman_collection.json`
- **Complete Postman collection** with 15+ test cases
- **Automated test assertions** that verify responses
- **Environment variables** for token management
- **Security tests** for unauthorized access attempts
- **CRUD operations testing** for task management

### **3. ⚡ Quick Test Script** - `quick_jwt_test.ps1`
- **One-click PowerShell script** for immediate testing
- **Visual feedback** with colors and emojis
- **Comprehensive test coverage** in under 30 seconds
- **Error handling** with detailed feedback
- **Summary report** of all test results

---

## 🚀 **How to Use These Resources**

### **Option 1: Quick PowerShell Test (Recommended for first-time testing)**

```powershell
# Navigate to your project directory
cd "C:\Users\prita\Documents\GitHub\ToDoList"

# Run the quick test script
powershell -ExecutionPolicy Bypass -File quick_jwt_test.ps1
```

**This will test:**
- ✅ JWT Login with admin credentials
- ✅ Protected endpoint access
- ✅ Task creation with authentication
- ✅ Token validation
- ✅ Authentication status check
- ✅ Invalid token rejection (security test)
- ✅ Token refresh functionality
- ✅ Task updates
- ✅ Logout process

### **Option 2: Import Postman Collection**

1. **Open Postman**
2. **Click Import** → **Choose Files**
3. **Select:** `JWT_TodoList_API_Complete.postman_collection.json`
4. **Run the collection** or individual requests
5. **Check test results** in the Test Results tab

**Collection Features:**
- 🔄 **Auto token management** - Tokens are automatically stored and reused
- 📊 **Built-in assertions** - Each request validates the response
- 🛡️ **Security testing** - Tests unauthorized access scenarios
- 📝 **Complete CRUD** - Full task management testing
- 🎯 **Role-based testing** - Admin vs User permission testing

### **Option 3: Manual Testing with Guide**

Follow the detailed instructions in `JWT_TESTING_GUIDE.md` for:
- Step-by-step manual testing
- Custom test scenarios
- Detailed expected responses
- Troubleshooting guidance

---

## 🔍 **Test Scenarios Covered**

### **🔐 Authentication Tests**
- [x] Admin login with JWT tokens
- [x] Regular user login
- [x] Token validation
- [x] Authentication status check
- [x] Token refresh with refresh token
- [x] Admin-only user registration
- [x] User logout

### **📝 Task Management Tests**
- [x] Get all tasks (protected endpoint)
- [x] Create new task with JWT auth
- [x] Get specific task by ID
- [x] Update existing task
- [x] Delete task

### **🛡️ Security Tests**
- [x] Access without token (should fail)
- [x] Access with invalid token (should fail)
- [x] Wrong login credentials (should fail)
- [x] User accessing admin endpoint (should fail)
- [x] Invalid refresh token (should fail)

### **📊 Audit & Logging Tests**
- [x] User activity logging
- [x] Security event logging
- [x] Method execution timing
- [x] Business operation tracking
- [x] Error logging

---

## 📋 **Sample Test Results**

### **Successful Login Response:**
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5Mjc4...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5Mjc4...",
    "tokenType": "Bearer",
    "username": "admin",
    "roles": ["ROLE_ADMIN"],
    "expiresIn": 86400,
    "issuedAt": "2025-08-23 15:30:45",
    "expiresAt": "2025-08-24 15:30:45"
}
```

### **Protected Endpoint Access:**
```bash
GET /tasks
Authorization: Bearer YOUR_TOKEN_HERE
→ Returns: Array of user's tasks
```

### **Security Test (Invalid Token):**
```bash
GET /tasks
Authorization: Bearer invalid-token
→ Returns: 401 Unauthorized
```

---

## 🎯 **Quick Start Commands**

### **Test Everything in 30 seconds:**
```powershell
powershell -ExecutionPolicy Bypass -File quick_jwt_test.ps1
```

### **Test Individual Endpoints:**
```powershell
# Login and get token
$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$token = $response.accessToken

# Access protected endpoint
$headers = @{ 'Authorization' = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Headers $headers

# Create new task
$task = '{"title":"Test Task","description":"Test","dueDate":"2025-08-30","status":"PENDING","priority":"HIGH"}'
Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method POST -Headers $headers -ContentType "application/json" -Body $task
```

### **Check Logs:**
```powershell
# View recent audit logs
Get-Content ".\logs\audit.log" -Tail 10

# View application logs
Get-Content ".\logs\application.log" -Tail 10

# View method execution logs
Get-Content ".\logs\method-execution.log" -Tail 10
```

---

## 🔧 **Environment Setup**

### **Required:**
- ✅ Application running on `http://localhost:8080`
- ✅ H2 database with test users loaded
- ✅ JWT configuration active

### **Available Test Users:**
```
admin / admin123 (ROLE_ADMIN)
john / password123 (ROLE_USER)
jane / password123 (ROLE_USER)
mike / password123 (ROLE_USER)
sarah / password123 (ROLE_USER)
```

### **Endpoints Available:**
```
Authentication:
POST /auth/login        - Login with credentials
POST /auth/refresh      - Refresh access token
POST /auth/validate     - Validate token
POST /auth/status       - Check auth status
POST /auth/register     - Register new user (admin only)
POST /auth/logout       - Logout user

Task Management:
GET    /tasks           - Get all tasks
POST   /tasks           - Create new task
GET    /tasks/{id}      - Get specific task
PUT    /tasks/{id}      - Update task
DELETE /tasks/{id}      - Delete task
```

---

## 🎉 **You're All Set!**

Run the quick test script to verify everything is working:

```powershell
powershell -ExecutionPolicy Bypass -File quick_jwt_test.ps1
```

This will give you immediate feedback on the JWT authentication system's functionality and generate comprehensive logs for audit purposes.

Happy Testing! 🧪✨
