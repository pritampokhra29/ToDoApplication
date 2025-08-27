# TodoList API

A Spring Boot REST API for managing tasks with user authentication.

## Quick Start

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Test the API:**
   ```powershell
   .\test_simple.ps1
   ```

## API Endpoints

### Authentication
- **POST** `/auth/login` - Login user
- **POST** `/auth/register` - Register new user

### Tasks (Base URL: `/tasks`)
- **GET** `/tasks` - Get all tasks (with optional `keyword` parameter)
- **POST** `/tasks/get` - Get task by ID (requires JSON: `{"id": taskId}`)
- **POST** `/tasks` - Create new task
- **POST** `/tasks/update` - Update task (requires JSON with `id` field)
- **POST** `/tasks/delete` - Delete task (requires JSON: `{"id": taskId}`)

## Request/Response Examples

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{"username":"admin","password":"admin123"}'
```

### Get All Tasks
```bash
curl -X GET "http://localhost:8080/tasks" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### Search Tasks
```bash
curl -X GET "http://localhost:8080/tasks?keyword=meeting" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

### Create Task
```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{"title":"New Task","description":"Task description","status":"PENDING"}'
```

### Get Task by ID
```bash
curl -X POST http://localhost:8080/tasks/get \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{"id": 1}'
```

### Update Task
```bash
curl -X POST http://localhost:8080/tasks/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{"id": 1, "title":"Updated Task","description":"Updated description","status":"IN_PROGRESS"}'
```

### Delete Task
```bash
curl -X POST http://localhost:8080/tasks/delete \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{"id": 1}'
```

## Testing

### Option 1: Quick Test (Recommended)
```powershell
.\test_simple.ps1
```

### Option 2: Comprehensive Test
```powershell
.\test_todolist_api.ps1
```

### Option 3: Final Verification
```powershell
.\final_verification.ps1
```

### Option 4: Postman Collection
Import `Fixed_TodoList_API_Collection.postman_collection.json` and set:
- `baseUrl`: `http://localhost:8080`

## Default User
- **Username:** admin
- **Password:** admin123

## Tech Stack
- Spring Boot 3.x
- Spring Security with JWT
- JPA/Hibernate
- H2 Database (in-memory)

## Deployment

This application can be deployed to various platforms. See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed instructions on deploying to Render with GitHub Actions.

## Common Issues

### 404 Errors
- Ensure base URL is `http://localhost:8080` (not `/tasks`)
- Tasks endpoints automatically have `/tasks` prefix

### Authentication
- Always include JWT token in Authorization header: `Bearer YOUR_TOKEN`
- Login first to get the token

### Status Values
- Valid statuses: `PENDING`, `IN_PROGRESS`, `COMPLETED`
