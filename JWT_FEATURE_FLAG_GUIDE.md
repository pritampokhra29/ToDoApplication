# 🔧 JWT Feature Flag Configuration Guide

## 📋 **Overview**

Your ToDo application now supports a **feature flag** to enable/disable JWT authentication. When JWT is disabled, the application automatically falls back to **Basic Authentication**.

---

## ⚙️ **Configuration**

### **Application Properties Setting**

In `src/main/resources/application.properties`:

```properties
# JWT Configuration
# Enable/Disable JWT Authentication (true = JWT, false = Basic Auth)
jwt.enabled=true
jwt.secret=JWTSecretKeyForToDoApplicationThatNeedsToBeAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400000
jwt.refresh.expiration=604800000
```

### **Available Values:**
- `jwt.enabled=true` → **JWT Authentication** (default)
- `jwt.enabled=false` → **Basic Authentication**

---

## 🔄 **How to Switch Authentication Methods**

### **Method 1: Properties File (Requires Restart)**

1. **Edit** `application.properties`
2. **Change** `jwt.enabled=false` (for Basic Auth) or `jwt.enabled=true` (for JWT)
3. **Restart** the application

### **Method 2: Environment Variable (No Restart Required)**

```bash
# Enable JWT
set JWT_ENABLED=true

# Disable JWT (use Basic Auth)
set JWT_ENABLED=false
```

### **Method 3: Command Line Parameter**

```bash
# Start with JWT enabled
java -jar app.jar --jwt.enabled=true

# Start with Basic Auth
java -jar app.jar --jwt.enabled=false
```

---

## 🔐 **Authentication Methods**

### **When JWT is ENABLED (`jwt.enabled=true`)**

#### **Endpoints Available:**
```
POST /auth/login       - Get JWT tokens
POST /auth/refresh     - Refresh access token
POST /auth/validate    - Validate token
POST /auth/status      - Check auth status
POST /auth/logout      - Logout
POST /auth/register    - Register user (admin only)
GET  /auth/config      - Check auth configuration
```

#### **Authentication Flow:**
1. **Login** → Get access & refresh tokens
2. **Use tokens** → Include `Authorization: Bearer TOKEN` in headers
3. **Refresh** → Use refresh token to get new access token

#### **Sample Request:**
```bash
# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Access protected endpoint
curl -X GET http://localhost:8080/tasks \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

### **When JWT is DISABLED (`jwt.enabled=false`)**

#### **Endpoints Available:**
```
POST /auth/register    - Register user (admin only)
GET  /auth/config      - Check auth configuration
```

#### **Authentication Method:**
- **HTTP Basic Authentication** only
- No JWT tokens needed
- Session-based authentication

#### **Sample Request:**
```bash
# Access protected endpoint with Basic Auth
curl -X GET http://localhost:8080/tasks \
  -u admin:admin123

# Or with explicit header
curl -X GET http://localhost:8080/tasks \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

---

## 🛡️ **Security Behavior**

### **JWT Mode Security:**
- ✅ **Stateless** authentication
- ✅ **Token-based** access control
- ✅ **Access & refresh** token support
- ✅ **Role-based** authorization
- ✅ **Session-free** operation

### **Basic Auth Mode Security:**
- ✅ **Session-based** authentication
- ✅ **Username/password** on each request
- ✅ **Role-based** authorization
- ✅ **Spring Security** default behavior
- ⚠️ **Stateful** sessions

---

## 📊 **Check Current Configuration**

### **Configuration Endpoint**
```bash
GET /auth/config
```

**Response when JWT is ENABLED:**
```json
{
    "jwtEnabled": true,
    "authenticationMethod": "JWT",
    "jwtEndpoints": [
        "/auth/login",
        "/auth/refresh", 
        "/auth/validate",
        "/auth/status",
        "/auth/logout"
    ],
    "message": "JWT Authentication is enabled"
}
```

**Response when JWT is DISABLED:**
```json
{
    "jwtEnabled": false,
    "authenticationMethod": "Basic Auth",
    "jwtEndpoints": ["Use HTTP Basic Authentication"],
    "message": "Basic Authentication is enabled"
}
```

---

## 🧪 **Testing Both Modes**

### **Test JWT Mode (jwt.enabled=true)**

```powershell
# Login and get token
$response = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$token = $response.accessToken

# Use token to access protected endpoint
$headers = @{ 'Authorization' = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Headers $headers
```

### **Test Basic Auth Mode (jwt.enabled=false)**

```powershell
# Use Basic Authentication
$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
$headers = @{ 'Authorization' = "Basic $credentials" }
Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Headers $headers

# Or use built-in credential parameter
Invoke-RestMethod -Uri "http://localhost:8080/tasks" -Method GET -Credential (Get-Credential)
```

---

## 🔄 **Migration Guide**

### **From Basic Auth to JWT:**
1. Set `jwt.enabled=true`
2. Restart application
3. Update client applications to use JWT endpoints
4. Use `/auth/login` to get tokens
5. Include `Authorization: Bearer TOKEN` in requests

### **From JWT to Basic Auth:**
1. Set `jwt.enabled=false`
2. Restart application
3. Update client applications to use Basic Auth
4. Include `Authorization: Basic CREDENTIALS` in requests
5. Remove JWT token logic from clients

---

## ⚠️ **Important Notes**

### **Behavior When Switching:**

1. **JWT → Basic Auth:**
   - All existing JWT tokens become invalid
   - JWT endpoints return `405 Method Not Allowed`
   - Must use Basic Authentication for all requests

2. **Basic Auth → JWT:**
   - All existing sessions are cleared
   - Must obtain new JWT tokens via `/auth/login`
   - JWT endpoints become available

### **Error Responses:**

**When JWT is disabled and JWT endpoints are called:**
```json
{
    "error": "JWT Authentication is disabled",
    "message": "Please use Basic Authentication instead",
    "authMethod": "Basic Auth"
}
```

**When JWT is enabled but Basic Auth is attempted:**
- Returns `401 Unauthorized` (no valid JWT token)

---

## 📝 **Configuration Examples**

### **Development Environment (JWT Enabled):**
```properties
jwt.enabled=true
jwt.secret=JWTSecretKeyForToDoApplicationThatNeedsToBeAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400000    # 24 hours
jwt.refresh.expiration=604800000  # 7 days
```

### **Legacy Environment (Basic Auth):**
```properties
jwt.enabled=false
# JWT settings are ignored when disabled
```

### **Production Environment (JWT with Custom Settings):**
```properties
jwt.enabled=true
jwt.secret=${JWT_SECRET_KEY}  # From environment variable
jwt.expiration=3600000        # 1 hour
jwt.refresh.expiration=86400000  # 24 hours
```

---

## 🎯 **Use Cases**

### **Use JWT When:**
- ✅ Building **modern web/mobile** applications
- ✅ Need **stateless** authentication
- ✅ Have **microservices** architecture
- ✅ Want **token-based** access control
- ✅ Need **refresh token** functionality

### **Use Basic Auth When:**
- ✅ **Legacy** applications integration
- ✅ **Simple** authentication requirements
- ✅ **Server-side rendered** applications
- ✅ **Testing** and development simplicity
- ✅ **Corporate environments** with Basic Auth standards

This feature flag gives you the flexibility to choose the authentication method that best fits your deployment environment and requirements! 🚀
