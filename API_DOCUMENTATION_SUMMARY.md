# API Documentation Summary

## Swagger/OpenAPI Documentation

The TodoList API now includes comprehensive API documentation using Swagger/OpenAPI 3.0. Once the application is running, you can access the interactive API documentation at:

### Documentation URLs
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

## Enhanced Features Added

### 1. Input Validation
- ✅ **Bean Validation**: Added comprehensive validation annotations to all DTOs
- ✅ **Global Exception Handler**: Centralized validation error handling
- ✅ **Standardized Error Responses**: Consistent error format across all endpoints
- ✅ **Field-level Validation**: Specific validation rules for each input field

### 2. API Documentation
- ✅ **Swagger Integration**: Interactive API documentation with try-it-out functionality
- ✅ **Operation Descriptions**: Detailed descriptions for each endpoint
- ✅ **Request/Response Examples**: Schema definitions and examples
- ✅ **Authentication Documentation**: JWT Bearer token authentication setup
- ✅ **Response Codes**: Documented all possible HTTP status codes and meanings

### 3. Security Enhancements
- ✅ **JWT Authentication**: Documented in Swagger with security schemes
- ✅ **Role-based Access**: Clear documentation of admin vs user endpoints
- ✅ **Input Sanitization**: Validation prevents malicious input

## Updated DTOs with Validation

### 1. UserDTO (Registration)
```java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 50)
@Pattern(regexp = "^[a-zA-Z0-9_]+$")
private String username;

@NotBlank(message = "Email is required")
@Email(message = "Email should be valid")
@Size(max = 100)
private String email;

@NotBlank(message = "Password is required")
@Size(min = 6, max = 100)
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")
private String password;

@Pattern(regexp = "^(USER|ADMIN)$")
private String role;
```

### 2. LoginDTO (Authentication)
```java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 50)
private String username;

@NotBlank(message = "Password is required")
@Size(min = 1, max = 100)
private String password;
```

### 3. UserUpdateDTO (Admin Updates)
```java
@NotNull(message = "User ID is required")
@Positive(message = "User ID must be positive")
private Long id;

@Size(min = 3, max = 50)
@Pattern(regexp = "^[a-zA-Z0-9_]*$")
private String username;

@Email(message = "Email should be valid")
@Size(max = 100)
private String email;

@Pattern(regexp = "^(USER|ADMIN)?$")
private String role;

@Size(min = 6, max = 100)
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$|^$")
private String password;
```

### 4. TaskDTO (Task Creation)
```java
@NotBlank(message = "Task title is required")
@Size(min = 1, max = 255)
private String title;

@Size(max = 2000)
private String description;

@Future(message = "Due date must be in the future")
private LocalDate dueDate;

@Pattern(regexp = "^(TODO|IN_PROGRESS|COMPLETED)$")
private String status;

@Size(max = 100)
private String category;

@Pattern(regexp = "^(LOW|MEDIUM|HIGH)$")
private String priority;
```

## API Endpoints with Documentation

### User Management Endpoints

#### POST /auth/register
- **Summary**: Register a new user
- **Authentication**: Required (JWT Bearer)
- **Role**: Any authenticated user (ADMIN role required for creating admin users)
- **Validation**: Full UserDTO validation
- **Responses**: 200 (Success), 400 (Validation Error), 401 (Unauthorized)

#### POST /auth/login
- **Summary**: User login
- **Authentication**: Not required
- **Validation**: LoginDTO validation
- **Responses**: 200 (Success with JWT), 400 (Validation Error), 401 (Invalid Credentials), 405 (JWT Disabled)

#### GET /auth/admin/users
- **Summary**: Get all users (Admin only)
- **Authentication**: Required (JWT Bearer)
- **Role**: ADMIN only
- **Responses**: 200 (Success), 401 (Unauthorized), 403 (Forbidden)

#### POST /auth/admin/users/{id}
- **Summary**: Update user (Admin only)
- **Authentication**: Required (JWT Bearer)
- **Role**: ADMIN only
- **Path Variable**: User ID (positive number)
- **Validation**: UserUpdateDTO validation
- **Responses**: 200 (Success), 400 (Validation Error), 401 (Unauthorized), 403 (Forbidden)

#### GET /auth/users/active
- **Summary**: Get active users for collaboration
- **Authentication**: Required (JWT Bearer)
- **Role**: Any authenticated user
- **Note**: Excludes the current logged-in user
- **Responses**: 200 (Success), 401 (Unauthorized), 500 (Server Error)

## Error Response Format

All validation errors follow this standardized format:

```json
{
    "success": false,
    "message": "Validation failed",
    "errors": {
        "fieldName": "Specific validation error message",
        "anotherField": "Another validation error message"
    },
    "timestamp": "2025-08-25T22:15:30",
    "status": 400
}
```

## Global Exception Handling

The API now handles these exception types:

1. **MethodArgumentNotValidException**: Bean validation failures
2. **ConstraintViolationException**: Constraint validation failures
3. **MethodArgumentTypeMismatchException**: Type conversion errors
4. **IllegalArgumentException**: Business logic validation errors
5. **BadCredentialsException**: Authentication failures
6. **RuntimeException**: Application-specific errors
7. **Exception**: Unexpected errors

## Fixed Issues

### 1. Duplicate Collaborator Prevention
- ✅ **Database-level Check**: Using `existsCollaboratorByTaskIdAndUserId()` query
- ✅ **Proper Error Handling**: Clear error messages for duplicate collaborators
- ✅ **Race Condition Prevention**: Database constraints prevent duplicates

### 2. User List Improvements
- ✅ **Exclude Current User**: Active users list excludes the logged-in user
- ✅ **Role-based Admin Creation**: Enhanced registration to support admin user creation
- ✅ **Status Display**: Admin users list shows active/inactive status

### 3. Security Enhancements
- ✅ **Inactive User Prevention**: Inactive users cannot login
- ✅ **Proper Authentication**: JWT tokens properly validated
- ✅ **Role-based Access Control**: Admin endpoints properly protected

## Testing the API

### Using Swagger UI
1. Start the application: `java -jar ToDo-0.0.1-SNAPSHOT.jar`
2. Open browser: `http://localhost:8080/swagger-ui/index.html`
3. Click "Authorize" and enter JWT token from login
4. Test any endpoint with the interactive interface

### Using Postman
- Import the provided Postman collections
- All requests now have proper validation
- Error responses are standardized and informative

### Sample Requests

#### User Registration
```json
POST /auth/register
{
    "username": "newuser123",
    "email": "user@example.com",
    "password": "SecurePass123",
    "role": "USER",
    "isActive": true
}
```

#### User Login
```json
POST /auth/login
{
    "username": "newuser123",
    "password": "SecurePass123"
}
```

#### Task Creation
```json
POST /tasks
{
    "title": "Complete project documentation",
    "description": "Finish writing the API documentation",
    "dueDate": "2025-08-30",
    "status": "TODO",
    "category": "Work",
    "priority": "HIGH"
}
```

## Next Steps

1. **Frontend Integration**: Use the validation guide to implement client-side validation
2. **Testing**: Test all endpoints with both valid and invalid data
3. **Documentation Review**: Review the Swagger documentation for completeness
4. **Production Deployment**: The API is now ready for production with proper validation and documentation

The API now provides a robust, well-documented, and validated interface for frontend applications to integrate with.
