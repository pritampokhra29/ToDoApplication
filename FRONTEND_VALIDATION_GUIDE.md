# API Input Validation Requirements

## Overview
This document outlines all the input validation rules implemented in the backend API. Frontend applications should implement matching client-side validations to provide immediate feedback to users and reduce server requests with invalid data.

## User Registration (`UserDTO`)

### Username
- **Required**: Yes
- **Type**: String
- **Min Length**: 3 characters
- **Max Length**: 50 characters
- **Pattern**: Only letters, numbers, and underscores (`^[a-zA-Z0-9_]+$`)
- **Error Messages**:
  - "Username is required" (if blank)
  - "Username must be between 3 and 50 characters"
  - "Username can only contain letters, numbers, and underscores"

### Email
- **Required**: Yes
- **Type**: String (valid email format)
- **Max Length**: 100 characters
- **Pattern**: Valid email format
- **Error Messages**:
  - "Email is required" (if blank)
  - "Email should be valid"
  - "Email must not exceed 100 characters"

### Password
- **Required**: Yes
- **Type**: String
- **Min Length**: 6 characters
- **Max Length**: 100 characters
- **Pattern**: Must contain at least one lowercase letter, one uppercase letter, and one digit (`^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$`)
- **Error Messages**:
  - "Password is required" (if blank)
  - "Password must be between 6 and 100 characters"
  - "Password must contain at least one lowercase letter, one uppercase letter, and one digit"

### Role (Optional)
- **Required**: No
- **Type**: String
- **Allowed Values**: "USER" or "ADMIN"
- **Pattern**: `^(USER|ADMIN)$`
- **Error Messages**:
  - "Role must be either USER or ADMIN"

### IsActive
- **Required**: No
- **Type**: Boolean
- **Default**: true (if not provided)

## User Login (`LoginDTO`)

### Username
- **Required**: Yes
- **Type**: String
- **Min Length**: 3 characters
- **Max Length**: 50 characters
- **Error Messages**:
  - "Username is required" (if blank)
  - "Username must be between 3 and 50 characters"

### Password
- **Required**: Yes
- **Type**: String
- **Min Length**: 1 character
- **Max Length**: 100 characters
- **Error Messages**:
  - "Password is required" (if blank)
  - "Password must not exceed 100 characters"

## User Update (`UserUpdateDTO`)

### ID
- **Required**: Yes
- **Type**: Long (positive number)
- **Constraint**: Must be positive
- **Error Messages**:
  - "User ID is required"
  - "User ID must be positive"

### Username (Optional for update)
- **Required**: No
- **Type**: String
- **Min Length**: 3 characters (if provided)
- **Max Length**: 50 characters
- **Pattern**: Only letters, numbers, and underscores (`^[a-zA-Z0-9_]*$`)
- **Error Messages**:
  - "Username must be between 3 and 50 characters"
  - "Username can only contain letters, numbers, and underscores"

### Email (Optional for update)
- **Required**: No
- **Type**: String (valid email format)
- **Max Length**: 100 characters
- **Error Messages**:
  - "Email should be valid"
  - "Email must not exceed 100 characters"

### Role (Optional for update)
- **Required**: No
- **Type**: String
- **Allowed Values**: "USER" or "ADMIN" (empty string allowed)
- **Pattern**: `^(USER|ADMIN)?$`
- **Error Messages**:
  - "Role must be either USER or ADMIN"

### Password (Optional for update)
- **Required**: No
- **Type**: String
- **Min Length**: 6 characters (if provided)
- **Max Length**: 100 characters
- **Pattern**: Must contain at least one lowercase letter, one uppercase letter, and one digit (or empty string) (`^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$|^$`)
- **Error Messages**:
  - "Password must be between 6 and 100 characters"
  - "Password must contain at least one lowercase letter, one uppercase letter, and one digit"

### IsActive (Optional for update)
- **Required**: No
- **Type**: Boolean

## Task Creation (`TaskDTO`)

### Title
- **Required**: Yes
- **Type**: String
- **Min Length**: 1 character
- **Max Length**: 255 characters
- **Error Messages**:
  - "Task title is required" (if blank)
  - "Title must be between 1 and 255 characters"

### Description (Optional)
- **Required**: No
- **Type**: String
- **Max Length**: 2000 characters
- **Error Messages**:
  - "Description must not exceed 2000 characters"

### Due Date (Optional)
- **Required**: No
- **Type**: Date (LocalDate format: YYYY-MM-DD)
- **Constraint**: Must be in the future
- **Error Messages**:
  - "Due date must be in the future"

### Status (Optional)
- **Required**: No
- **Type**: String
- **Allowed Values**: "TODO", "IN_PROGRESS", "COMPLETED"
- **Pattern**: `^(TODO|IN_PROGRESS|COMPLETED)$`
- **Error Messages**:
  - "Status must be TODO, IN_PROGRESS, or COMPLETED"

### Category (Optional)
- **Required**: No
- **Type**: String
- **Max Length**: 100 characters
- **Error Messages**:
  - "Category must not exceed 100 characters"

### Priority (Optional)
- **Required**: No
- **Type**: String
- **Allowed Values**: "LOW", "MEDIUM", "HIGH"
- **Pattern**: `^(LOW|MEDIUM|HIGH)$`
- **Error Messages**:
  - "Priority must be LOW, MEDIUM, or HIGH"

## Path Variable Validations

### User ID (in URLs like `/admin/users/{id}`)
- **Required**: Yes
- **Type**: Long (positive number)
- **Constraint**: Must be positive
- **Error Messages**:
  - "User ID must be positive"

### Task ID (in URLs like `/tasks/{id}`)
- **Required**: Yes
- **Type**: Long (positive number)
- **Constraint**: Must be positive
- **Error Messages**:
  - "Task ID must be positive"

## General Validation Rules

### Trimming
- All string inputs should be trimmed of leading and trailing whitespace before validation
- Empty strings after trimming are considered invalid for required fields

### Case Sensitivity
- Role values ("USER", "ADMIN") are case-sensitive - must be uppercase
- Status values ("TODO", "IN_PROGRESS", "COMPLETED") are case-sensitive - must be uppercase
- Priority values ("LOW", "MEDIUM", "HIGH") are case-sensitive - must be uppercase
- Email validation is case-insensitive

### Null vs Empty String
- Required fields cannot be null or empty/blank
- Optional fields can be null but if provided as string, they cannot be empty/blank (except where explicitly allowed)

## Frontend Implementation Guidelines

### Client-Side Validation Order
1. **Required Field Check**: Validate all required fields are provided
2. **Format Validation**: Check string patterns, email format, etc.
3. **Length Validation**: Validate min/max length constraints
4. **Business Logic Validation**: Check date constraints, enum values, etc.

### User Experience Recommendations
1. **Real-time Validation**: Validate fields as user types (with debouncing)
2. **Field-level Feedback**: Show validation errors for each field individually
3. **Form-level Validation**: Validate entire form before submission
4. **Clear Error Messages**: Use the exact error messages provided above
5. **Positive Feedback**: Show green checkmarks or success indicators for valid fields

### Error Handling
- Display validation errors immediately when field loses focus
- Highlight invalid fields with red borders or error styling
- Show error messages below or next to the invalid field
- Prevent form submission if any validation errors exist
- Clear error messages when user starts correcting the field

### Example Frontend Validation (JavaScript/TypeScript)

```javascript
// Username validation example
function validateUsername(username) {
    const errors = [];
    
    if (!username || username.trim() === '') {
        errors.push('Username is required');
    } else {
        const trimmed = username.trim();
        if (trimmed.length < 3 || trimmed.length > 50) {
            errors.push('Username must be between 3 and 50 characters');
        }
        if (!/^[a-zA-Z0-9_]+$/.test(trimmed)) {
            errors.push('Username can only contain letters, numbers, and underscores');
        }
    }
    
    return errors;
}

// Password validation example
function validatePassword(password) {
    const errors = [];
    
    if (!password || password.trim() === '') {
        errors.push('Password is required');
    } else {
        if (password.length < 6 || password.length > 100) {
            errors.push('Password must be between 6 and 100 characters');
        }
        if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/.test(password)) {
            errors.push('Password must contain at least one lowercase letter, one uppercase letter, and one digit');
        }
    }
    
    return errors;
}

// Email validation example
function validateEmail(email) {
    const errors = [];
    
    if (!email || email.trim() === '') {
        errors.push('Email is required');
    } else {
        const trimmed = email.trim();
        if (trimmed.length > 100) {
            errors.push('Email must not exceed 100 characters');
        }
        // Simple email regex - you can use a more comprehensive one
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmed)) {
            errors.push('Email should be valid');
        }
    }
    
    return errors;
}
```

## API Response Format for Validation Errors

When validation fails, the API returns a standardized error response:

```json
{
    "success": false,
    "message": "Validation failed",
    "errors": {
        "username": "Username must be between 3 and 50 characters",
        "email": "Email should be valid",
        "password": "Password must contain at least one lowercase letter, one uppercase letter, and one digit"
    },
    "timestamp": "2025-08-25T22:15:30",
    "status": 400
}
```

The frontend should parse this response and display the field-specific errors to the user.

## Additional Notes

1. **Security**: Client-side validation is for UX only - all validation is enforced on the server
2. **Consistency**: Keep frontend validation rules exactly matching backend rules
3. **Updates**: When backend validation rules change, update frontend accordingly
4. **Testing**: Test both valid and invalid inputs to ensure validation works correctly
5. **Accessibility**: Ensure validation messages are accessible to screen readers

This validation framework ensures data integrity and provides a consistent user experience across the application.
