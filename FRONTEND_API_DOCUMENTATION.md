# 📋 **ToDoList API Documentation**

**Version:** 1.0.0  
**Base URL:** `http://localhost:8080` (Development) | `https://your-domain.com` (Production)  
**Date:** August 27, 2025

---

## 🚨 **IMPORTANT CHANGES**

### **✅ Major Update: Collaborators Now Included in Main Endpoints**

**What Changed:**
- `GET /tasks` now returns **TaskResponse** objects with full collaborator details
- `GET /tasks/{id}` now returns **TaskResponse** objects with full collaborator details
- Previous behavior moved to `/basic` endpoints for backward compatibility

**Impact on Frontend:**
- **✅ BENEFIT:** No need for separate API calls to get collaborators
- **✅ BENEFIT:** Consistent data model across all endpoints
- **⚠️ BREAKING:** Response structure changed from `Task` to `TaskResponse`

**Migration Guide:**
- **Old:** `task.user` → **New:** `task.owner`
- **Old:** No collaborators → **New:** `task.collaborators` array available
- **Legacy endpoints:** Use `/tasks/basic` and `/tasks/{id}/basic` if you need old format

---

## 🔐 **Authentication**

All endpoints (except registration and login) require JWT authentication:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## 📚 **Table of Contents**

1. [Authentication Endpoints](#-authentication-endpoints)
2. [Task Management Endpoints](#-task-management-endpoints)
3. [Collaboration Endpoints](#-collaboration-endpoints)
4. [User Management Endpoints](#-user-management-endpoints)
5. [Data Models](#-data-models)
6. [Error Responses](#-error-responses)

---

## 🔐 **Authentication Endpoints**

### **1. User Registration**
**Endpoint:** `POST /auth/register`  
**Description:** Register a new user (Admin access required)  
**Authentication:** Required (Admin role)

**Request:**
```json
{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "role": "USER"
}
```

**Response (Success):**
```json
{
    "message": "User registered successfully",
    "userId": 6,
    "username": "newuser"
}
```

**Response (Error):**
```json
{
    "error": "Username already exists"
}
```

---

### **2. User Login**
**Endpoint:** `POST /auth/login`  
**Description:** Authenticate user and get JWT token  
**Authentication:** None

**Request:**
```json
{
    "username": "admin",
    "password": "admin123"
}
```

**Response (Success):**
```json
{
    "message": "Login successful",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
        "id": 1,
        "username": "admin",
        "email": "admin@todolist.com",
        "role": "ADMIN",
        "isActive": true
    },
    "expiresIn": 86400000
}
```

**Response (Error):**
```json
{
    "error": "Invalid credentials"
}
```

---

### **3. Token Validation**
**Endpoint:** `POST /auth/validate`  
**Description:** Validate JWT token  
**Authentication:** Required

**Request:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (Success):**
```json
{
    "valid": true,
    "username": "admin",
    "expiresAt": "2025-08-28T14:30:00Z"
}
```

---

### **4. Token Refresh**
**Endpoint:** `POST /auth/refresh`  
**Description:** Refresh JWT token  
**Authentication:** Required

**Request:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (Success):**
```json
{
    "message": "Token refreshed successfully",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000
}
```

---

### **5. User Logout**
**Endpoint:** `POST /auth/logout`  
**Description:** Logout user and blacklist token  
**Authentication:** Required

**Request:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (Success):**
```json
{
    "message": "Logged out successfully"
}
```

---

## 📝 **Task Management Endpoints**

### **1. Create Task (Basic)**
**Endpoint:** `POST /tasks`  
**Description:** Create a new task  
**Authentication:** Required

**Request:**
```json
{
    "title": "Complete Project Proposal",
    "description": "Finish the Q4 project proposal document",
    "dueDate": "2025-09-15",
    "status": "PENDING",
    "category": "Work",
    "priority": "HIGH"
}
```

**Response (Success):**
```json
{
    "id": 25,
    "title": "Complete Project Proposal",
    "description": "Finish the Q4 project proposal document",
    "dueDate": "2025-09-15",
    "status": "PENDING",
    "createDate": "2025-08-27",
    "updateDate": "2025-08-27",
    "deleted": false,
    "completionDate": null,
    "category": "Work",
    "priority": "HIGH",
    "user": {
        "id": 2,
        "username": "john",
        "email": "john@example.com",
        "role": "USER",
        "isActive": true
    }
}
```

---

### **2. Create Task with Collaborators (NEW)**
**Endpoint:** `POST /tasks/with-collaborators`  
**Description:** Create a new task with collaborators in single request  
**Authentication:** Required

**Request (Option 1 - Using Usernames):**
```json
{
    "title": "Database Migration Project",
    "description": "Migrate production database to PostgreSQL",
    "dueDate": "2025-09-10",
    "status": "PENDING",
    "category": "Work",
    "priority": "HIGH",
    "collaboratorUsernames": ["jane", "mike", "sarah"]
}
```

**Request (Option 2 - Using User IDs):**
```json
{
    "title": "Code Review Session",
    "description": "Review pull requests for authentication module",
    "dueDate": "2025-09-05",
    "status": "PENDING",
    "category": "Work",
    "priority": "HIGH",
    "collaboratorUserIds": [3, 4, 5]
}
```

**Response (Success):**
```json
{
    "id": 26,
    "title": "Database Migration Project",
    "description": "Migrate production database to PostgreSQL",
    "dueDate": "2025-09-10",
    "status": "PENDING",
    "createDate": "2025-08-27",
    "updateDate": "2025-08-27",
    "deleted": false,
    "completionDate": null,
    "category": "Work",
    "priority": "HIGH",
    "owner": {
        "id": 2,
        "username": "john",
        "email": "john@example.com",
        "role": "USER",
        "isActive": true
    },
    "collaborators": [
        {
            "id": 2,
            "username": "john",
            "email": "john@example.com",
            "role": "USER",
            "isActive": true
        },
        {
            "id": 3,
            "username": "jane",
            "email": "jane@example.com",
            "role": "USER",
            "isActive": true
        },
        {
            "id": 4,
            "username": "mike",
            "email": "mike@example.com",
            "role": "USER",
            "isActive": true
        },
        {
            "id": 5,
            "username": "sarah",
            "email": "sarah@example.com",
            "role": "USER",
            "isActive": true
        }
    ]
}
```

---

### **3. Get All Tasks**
**Endpoint:** `GET /tasks`  
**Description:** Get all tasks for the authenticated user (owned + collaborated) **WITH collaborator details**  
**Authentication:** Required

**Request:** No body required

**Response (Success):**
```json
[
    {
        "id": 1,
        "title": "Complete Project Proposal",
        "description": "Finish the Q4 project proposal document",
        "dueDate": "2025-09-15",
        "status": "IN_PROGRESS",
        "createDate": "2025-08-20",
        "updateDate": "2025-08-27",
        "deleted": false,
        "completionDate": null,
        "category": "Work",
        "priority": "HIGH",
        "owner": {
            "id": 2,
            "username": "john",
            "email": "john@example.com",
            "role": "USER",
            "isActive": true
        },
        "collaborators": [
            {
                "id": 2,
                "username": "john",
                "email": "john@example.com",
                "role": "USER",
                "isActive": true
            },
            {
                "id": 3,
                "username": "jane",
                "email": "jane@example.com",
                "role": "USER",
                "isActive": true
            }
        ]
    },
    {
        "id": 5,
        "title": "Database Migration",
        "description": "Collaborative task where user is collaborator",
        "dueDate": "2025-09-01",
        "status": "PENDING",
        "createDate": "2025-08-21",
        "updateDate": "2025-08-21",
        "deleted": false,
        "completionDate": null,
        "category": "Work",
        "priority": "HIGH",
        "owner": {
            "id": 3,
            "username": "jane",
            "email": "jane@example.com",
            "role": "USER",
            "isActive": true
        },
        "collaborators": [
            {
                "id": 3,
                "username": "jane",
                "email": "jane@example.com",
                "role": "USER",
                "isActive": true
            },
            {
                "id": 2,
                "username": "john",
                "email": "john@example.com",
                "role": "USER",
                "isActive": true
            }
        ]
    }
]
```

---

### **3a. Get All Tasks (Legacy - Without Collaborators)**
**Endpoint:** `GET /tasks/basic`  
**Description:** Get all tasks without collaborator details (for backward compatibility)  
**Authentication:** Required

**Request:** No body required

**Response (Success):** Array of basic Task objects without collaborator details

---

### **4. Get Task by ID**
**Endpoint:** `GET /tasks/{id}`  
**Description:** Get a specific task by ID **WITH collaborator details**  
**Authentication:** Required

**Request:** No body required

**Response (Success):**
```json
{
    "id": 1,
    "title": "Complete Project Proposal",
    "description": "Finish the Q4 project proposal document",
    "dueDate": "2025-09-15",
    "status": "IN_PROGRESS",
    "createDate": "2025-08-20",
    "updateDate": "2025-08-27",
    "deleted": false,
    "completionDate": null,
    "category": "Work",
    "priority": "HIGH",
    "owner": {
        "id": 2,
        "username": "john",
        "email": "john@example.com",
        "role": "USER",
        "isActive": true
    },
    "collaborators": [
        {
            "id": 2,
            "username": "john",
            "email": "john@example.com",
            "role": "USER",
            "isActive": true
        },
        {
            "id": 3,
            "username": "jane",
            "email": "jane@example.com",
            "role": "USER",
            "isActive": true
        }
    ]
}
```

**Response (Error):**
```json
{
    "error": "Task not found or access denied"
}
```

---

### **4a. Get Task by ID (Legacy - Without Collaborators)**
**Endpoint:** `GET /tasks/{id}/basic`  
**Description:** Get a specific task by ID without collaborator details  
**Authentication:** Required

**Request:** No body required

**Response (Success):** Basic Task object without collaborator details

---

### **5. Update Task (Basic)**
**Endpoint:** `POST /tasks/update`  
**Description:** Update an existing task  
**Authentication:** Required

**Request:**
```json
{
    "id": 1,
    "title": "Complete Project Proposal - Updated",
    "description": "Updated description",
    "dueDate": "2025-09-20",
    "status": "IN_PROGRESS",
    "category": "Work",
    "priority": "MEDIUM"
}
```

**Response (Success):**
```json
{
    "id": 1,
    "title": "Complete Project Proposal - Updated",
    "description": "Updated description",
    "dueDate": "2025-09-20",
    "status": "IN_PROGRESS",
    "createDate": "2025-08-20",
    "updateDate": "2025-08-27",
    "deleted": false,
    "completionDate": null,
    "category": "Work",
    "priority": "MEDIUM",
    "user": {
        "id": 2,
        "username": "john",
        "email": "john@example.com",
        "role": "USER",
        "isActive": true
    }
}
```

---

### **6. Update Task with Collaborators (NEW)**
**Endpoint:** `POST /tasks/update-with-collaborators`  
**Description:** Update task and collaborators in single request  
**Authentication:** Required

**Request:**
```json
{
    "id": 1,
    "title": "Complete Project Proposal - Enhanced",
    "description": "Updated with new requirements",
    "dueDate": "2025-09-25",
    "status": "IN_PROGRESS",
    "category": "Work",
    "priority": "HIGH",
    "collaboratorUserIds": [3, 4, 5]
}
```

**Response (Success):**
```json
{
    "id": 1,
    "title": "Complete Project Proposal - Enhanced",
    "description": "Updated with new requirements",
    "dueDate": "2025-09-25",
    "status": "IN_PROGRESS",
    "createDate": "2025-08-20",
    "updateDate": "2025-08-27",
    "deleted": false,
    "completionDate": null,
    "category": "Work",
    "priority": "HIGH",
    "owner": {
        "id": 2,
        "username": "john",
        "email": "john@example.com",
        "role": "USER",
        "isActive": true
    },
    "collaborators": [
        {
            "id": 2,
            "username": "john",
            "email": "john@example.com",
            "role": "USER",
            "isActive": true
        },
        {
            "id": 3,
            "username": "jane",
            "email": "jane@example.com",
            "role": "USER",
            "isActive": true
        },
        {
            "id": 4,
            "username": "mike",
            "email": "mike@example.com",
            "role": "USER",
            "isActive": true
        },
        {
            "id": 5,
            "username": "sarah",
            "email": "sarah@example.com",
            "role": "USER",
            "isActive": true
        }
    ]
}
```

---

### **7. Delete Task**
**Endpoint:** `DELETE /tasks/{id}`  
**Description:** Delete a task (soft delete)  
**Authentication:** Required

**Request:** No body required

**Response (Success):**
```json
{
    "message": "Task deleted successfully"
}
```

---

### **8. Search Tasks**
**Endpoint:** `GET /tasks/search?query={searchTerm}`  
**Description:** Search tasks by title or description  
**Authentication:** Required

**Query Parameters:**
- `query` (required): Search term

**Response (Success):**
```json
[
    {
        "id": 1,
        "title": "Complete Project Proposal",
        "description": "Finish the Q4 project proposal document",
        "dueDate": "2025-09-15",
        "status": "IN_PROGRESS",
        "createDate": "2025-08-20",
        "updateDate": "2025-08-27",
        "deleted": false,
        "completionDate": null,
        "category": "Work",
        "priority": "HIGH",
        "user": {
            "id": 2,
            "username": "john",
            "email": "john@example.com",
            "role": "USER",
            "isActive": true
        }
    }
]
```

---

### **9. Filter Tasks by Status**
**Endpoint:** `GET /tasks/filter/status?status={status}`  
**Description:** Filter tasks by status  
**Authentication:** Required

**Query Parameters:**
- `status` (required): `PENDING`, `IN_PROGRESS`, or `COMPLETED`

**Response (Success):** Array of tasks with specified status

---

### **10. Filter Tasks by Priority**
**Endpoint:** `GET /tasks/filter/priority?priority={priority}`  
**Description:** Filter tasks by priority  
**Authentication:** Required

**Query Parameters:**
- `priority` (required): `LOW`, `MEDIUM`, or `HIGH`

**Response (Success):** Array of tasks with specified priority

---

### **11. Get Tasks Due Today**
**Endpoint:** `GET /tasks/due-today`  
**Description:** Get all tasks due today  
**Authentication:** Required

**Request:** No body required

**Response (Success):** Array of tasks due today

---

## 👥 **Collaboration Endpoints**

### **1. Add Collaborator by Username**
**Endpoint:** `POST /tasks/collaborator/add`  
**Description:** Add a collaborator to an existing task  
**Authentication:** Required

**Request:**
```json
{
    "taskId": 1,
    "collaboratorUsername": "jane"
}
```

**Response (Success):**
```json
{
    "id": 1,
    "title": "Complete Project Proposal",
    "description": "Finish the Q4 project proposal document",
    "dueDate": "2025-09-15",
    "status": "IN_PROGRESS",
    "createDate": "2025-08-20",
    "updateDate": "2025-08-27",
    "deleted": false,
    "completionDate": null,
    "category": "Work",
    "priority": "HIGH",
    "user": {
        "id": 2,
        "username": "john",
        "email": "john@example.com",
        "role": "USER",
        "isActive": true
    }
}
```

---

### **2. Add Collaborator by User ID**
**Endpoint:** `POST /tasks/collaborator/add/by-id`  
**Description:** Add a collaborator to an existing task using user ID  
**Authentication:** Required

**Request:**
```json
{
    "taskId": 1,
    "collaboratorUserId": 3
}
```

**Response (Success):** Same as above

---

### **3. Remove Collaborator**
**Endpoint:** `POST /tasks/collaborator/remove`  
**Description:** Remove a collaborator from a task  
**Authentication:** Required

**Request:**
```json
{
    "taskId": 1,
    "collaboratorUsername": "jane"
}
```

**Response (Success):** Updated task without the removed collaborator

---

## 👤 **User Management Endpoints**

### **1. Get Active Users**
**Endpoint:** `GET /auth/users/active`  
**Description:** Get list of all active users  
**Authentication:** Required

**Request:** No body required

**Response (Success):**
```json
[
    {
        "id": 1,
        "username": "admin",
        "email": "admin@todolist.com",
        "role": "ADMIN",
        "isActive": true
    },
    {
        "id": 2,
        "username": "john",
        "email": "john@example.com",
        "role": "USER",
        "isActive": true
    }
]
```

---

### **2. Get Configuration**
**Endpoint:** `GET /auth/config`  
**Description:** Get application configuration  
**Authentication:** Required

**Request:** No body required

**Response (Success):**
```json
{
    "appName": "ToDoList Application",
    "version": "1.0.0",
    "jwtEnabled": true,
    "maxLoginAttempts": 5,
    "tokenExpirationTime": 86400000
}
```

---

## 📊 **Data Models**

### **Task Model**
```json
{
    "id": "number",
    "title": "string (required)",
    "description": "string (optional)",
    "dueDate": "string (YYYY-MM-DD format, optional)",
    "status": "string (PENDING|IN_PROGRESS|COMPLETED, optional, default: PENDING)",
    "createDate": "string (YYYY-MM-DD, auto-generated)",
    "updateDate": "string (YYYY-MM-DD, auto-generated)",
    "deleted": "boolean (auto-generated, default: false)",
    "completionDate": "string (YYYY-MM-DD, optional)",
    "category": "string (optional)",
    "priority": "string (LOW|MEDIUM|HIGH, optional, default: MEDIUM)",
    "user": "User object (auto-assigned)"
}
```

### **TaskResponse Model (Enhanced)**
```json
{
    "id": "number",
    "title": "string",
    "description": "string",
    "dueDate": "string (YYYY-MM-DD)",
    "status": "string",
    "createDate": "string (YYYY-MM-DD)",
    "updateDate": "string (YYYY-MM-DD)",
    "deleted": "boolean",
    "completionDate": "string (YYYY-MM-DD)",
    "category": "string",
    "priority": "string",
    "owner": "UserDTO object",
    "collaborators": "Array of UserDTO objects"
}
```

### **User Model**
```json
{
    "id": "number",
    "username": "string (required)",
    "email": "string (required)",
    "password": "string (required for registration)",
    "role": "string (USER|ADMIN, optional, default: USER)",
    "isActive": "boolean"
}
```

### **UserDTO Model**
```json
{
    "id": "number",
    "username": "string",
    "email": "string",
    "role": "string",
    "isActive": "boolean"
}
```

---

## ❌ **Error Responses**

### **400 - Bad Request**
```json
{
    "error": "Validation failed",
    "details": "Title is required"
}
```

### **401 - Unauthorized**
```json
{
    "error": "Authentication required"
}
```

### **403 - Forbidden**
```json
{
    "error": "Access denied"
}
```

### **404 - Not Found**
```json
{
    "error": "Task not found or access denied"
}
```

### **500 - Internal Server Error**
```json
{
    "error": "Internal server error",
    "message": "An unexpected error occurred"
}
```

---

## 🔑 **Important Notes**

### **Authentication:**
- All endpoints (except `/auth/register` and `/auth/login`) require JWT token
- Token should be included in `Authorization: Bearer <token>` header
- Tokens expire after 24 hours (configurable)

### **Task Access Control:**
- Users can only access tasks they own or collaborate on
- Task owners can add/remove collaborators
- Collaborators can view and update tasks but cannot delete them

### **Date Format:**
- All dates use ISO format: `YYYY-MM-DD`
- Times are handled server-side

### **Status Values:**
- `PENDING`: Task not started
- `IN_PROGRESS`: Task is being worked on
- `COMPLETED`: Task is finished

### **Priority Values:**
- `LOW`: Low priority task
- `MEDIUM`: Medium priority task (default)
- `HIGH`: High priority task

### **Role Values:**
- `USER`: Regular user (default)
- `ADMIN`: Administrator with elevated privileges

---

## 🆕 **New vs Legacy Endpoints**

### **Recommended (New) Endpoints:**
- `POST /tasks/with-collaborators` - Create task with collaborators
- `POST /tasks/update-with-collaborators` - Update task with collaborators

### **Legacy Endpoints (Still Supported):**
- `POST /tasks` - Create basic task
- `POST /tasks/update` - Update basic task
- `POST /tasks/collaborator/add` - Add collaborator separately

### **Benefits of New Endpoints:**
- ✅ Single API call for task + collaborators
- ✅ Better performance (fewer round trips)
- ✅ Atomic operations (all or nothing)
- ✅ Rich response with collaborator details
- ✅ Consistent data models

---

## 📞 **Support**

For any discrepancies or questions, please contact the backend team or create an issue in the project repository.

**Last Updated:** August 27, 2025  
**API Version:** 1.0.0
