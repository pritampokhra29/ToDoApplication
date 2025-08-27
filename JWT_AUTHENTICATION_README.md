# JWT Authentication Implementation

## Overview
This application implements JWT (JSON Web Token) authentication to provide secure, stateless authentication with improved user experience. The implementation includes access tokens, refresh tokens, comprehensive logging, and security monitoring.

## Features

### 🔐 **JWT Authentication System**
- **Access Tokens**: Short-lived tokens (24 hours) for API access
- **Refresh Tokens**: Long-lived tokens (7 days) for token renewal
- **Stateless Authentication**: No server-side session storage required
- **Role-based Authorization**: Support for user roles and permissions
- **Token Validation**: Comprehensive token validation with expiry checks

### 🛡️ **Security Features**
- **HMAC SHA-256 Signing**: Secure token signing with configurable secret
- **Token Type Validation**: Separate access and refresh token types
- **IP Address Tracking**: Client IP logging for audit purposes
- **Failed Authentication Monitoring**: Security event logging
- **Token Expiry Management**: Automatic token expiration handling

### 📊 **Comprehensive Logging**
- **Authentication Events**: Login, logout, token refresh logging
- **Security Events**: Failed authentication attempts, invalid tokens
- **User Activity**: Complete audit trail of user actions
- **Performance Monitoring**: Token validation performance tracking

## API Endpoints

### 1. **Login** - `POST /auth/login`
Authenticate user and receive JWT tokens.

**Request:**
```json
{
    "username": "john.doe",
    "password": "password123"
}
```

**Response:**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "username": "john.doe",
    "roles": ["ROLE_USER"],
    "expiresIn": 86400,
    "issuedAt": "2023-12-01 10:30:45",
    "expiresAt": "2023-12-02 10:30:45"
}
```

### 2. **Refresh Token** - `POST /auth/refresh`
Get new access token using refresh token.

**Request:**
```json
{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "username": "john.doe",
    "roles": ["ROLE_USER"],
    "expiresIn": 86400,
    "issuedAt": "2023-12-01 11:30:45",
    "expiresAt": "2023-12-02 11:30:45"
}
```

### 3. **Validate Token** - `POST /auth/validate`
Validate token and get user information.

**Request Header:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
    "valid": true,
    "username": "john.doe",
    "remainingValiditySeconds": 82800
}
```

### 4. **Authentication Status** - `POST /auth/status`
Check current authentication status.

**Response:**
```json
{
    "authenticated": true,
    "username": "john.doe",
    "authorities": [
        {
            "authority": "ROLE_USER"
        }
    ]
}
```

### 5. **Logout** - `POST /auth/logout`
Clear authentication context (client should discard tokens).

**Response:**
```json
{
    "message": "Logout successful"
}
```

## Usage Examples

### Frontend Integration

#### 1. **Login and Store Tokens**
```javascript
// Login
const loginResponse = await fetch('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'john.doe', password: 'password123' })
});

const tokens = await loginResponse.json();

// Store tokens securely
localStorage.setItem('accessToken', tokens.accessToken);
localStorage.setItem('refreshToken', tokens.refreshToken);
```

#### 2. **Make Authenticated Requests**
```javascript
// Add Authorization header to requests
const response = await fetch('/tasks', {
    method: 'GET',
    headers: {
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`,
        'Content-Type': 'application/json'
    }
});

if (response.status === 401) {
    // Token expired, try refresh
    await refreshAccessToken();
}
```

#### 3. **Refresh Token Implementation**
```javascript
async function refreshAccessToken() {
    try {
        const refreshResponse = await fetch('/auth/refresh', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                refreshToken: localStorage.getItem('refreshToken') 
            })
        });

        if (refreshResponse.ok) {
            const newTokens = await refreshResponse.json();
            localStorage.setItem('accessToken', newTokens.accessToken);
            return true;
        } else {
            // Refresh failed, redirect to login
            window.location.href = '/login';
            return false;
        }
    } catch (error) {
        console.error('Token refresh failed:', error);
        window.location.href = '/login';
        return false;
    }
}
```

#### 4. **Automatic Token Refresh**
```javascript
// Axios interceptor example
axios.interceptors.response.use(
    response => response,
    async error => {
        if (error.response?.status === 401) {
            const refreshed = await refreshAccessToken();
            if (refreshed) {
                // Retry original request
                error.config.headers.Authorization = 
                    `Bearer ${localStorage.getItem('accessToken')}`;
                return axios.request(error.config);
            }
        }
        return Promise.reject(error);
    }
);
```

## Configuration

### Application Properties
```properties
# JWT Configuration
jwt.secret=JWTSecretKeyForToDoApplicationThatNeedsToBeAtLeast256BitsLongForHS256Algorithm
jwt.expiration=86400000              # 24 hours in milliseconds
jwt.refresh.expiration=604800000     # 7 days in milliseconds
```

### Security Configuration
- **Stateless Sessions**: No server-side session storage
- **CORS Support**: Configure for frontend integration
- **CSRF Disabled**: Not needed for stateless JWT authentication
- **Permitted Endpoints**: Login, refresh, and H2 console for development

## Token Structure

### Access Token Claims
```json
{
    "sub": "john.doe",
    "iat": 1701422445,
    "exp": 1701508845,
    "roles": ["ROLE_USER"],
    "tokenType": "ACCESS"
}
```

### Refresh Token Claims
```json
{
    "sub": "john.doe",
    "iat": 1701422445,
    "exp": 1702027245,
    "tokenType": "REFRESH"
}
```

## Security Best Practices

### 1. **Token Storage**
- **Frontend**: Store in memory or secure httpOnly cookies
- **Avoid**: localStorage for production (XSS vulnerability)
- **Mobile**: Use secure keychain/keystore

### 2. **Token Transmission**
- **HTTPS Only**: Always use HTTPS in production
- **Authorization Header**: `Bearer <token>` format
- **No URL Parameters**: Avoid tokens in URLs (logged in server logs)

### 3. **Token Lifecycle**
- **Short Access Token**: 15-60 minutes for high security
- **Longer Refresh Token**: 7-30 days
- **Rotation**: Consider refresh token rotation for maximum security

### 4. **Error Handling**
- **Graceful Degradation**: Handle token expiry gracefully
- **Retry Logic**: Automatic token refresh on 401 errors
- **User Feedback**: Clear error messages for authentication failures

## Monitoring and Logging

### Security Events Logged
- Authentication attempts (success/failure)
- Token generation and refresh
- Invalid token usage attempts
- Token validation failures
- User login/logout activities

### Audit Information
- **Username**: Who performed the action
- **IP Address**: Where the request came from
- **Timestamp**: When the action occurred
- **Action Type**: What action was performed
- **Result**: Success or failure with details

### Log Files
- **audit.log**: Security and user activity events
- **application.log**: General application logs
- **error.log**: Error-specific logs with stack traces

## Testing

### 1. **Login Test**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

### 2. **Protected Endpoint Test**
```bash
curl -X GET http://localhost:8080/tasks \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. **Token Refresh Test**
```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"YOUR_REFRESH_TOKEN"}'
```

### 4. **Token Validation Test**
```bash
curl -X POST http://localhost:8080/auth/validate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Troubleshooting

### Common Issues

1. **"Invalid or expired token"**
   - Check token expiry time
   - Verify token format (Bearer prefix)
   - Ensure token hasn't been tampered with

2. **"Authentication failed"**
   - Verify username/password combination
   - Check if user account is active
   - Review authentication logs

3. **"Token refresh failed"**
   - Ensure refresh token is valid
   - Check if refresh token has expired
   - Verify token type is "REFRESH"

### Debug Tips
- Enable DEBUG logging for JWT classes
- Check server logs for detailed error messages
- Use JWT decoder tools to inspect token contents
- Monitor network requests in browser developer tools

## Migration from Basic Auth

1. **Update Frontend**: Replace Basic Auth with JWT token handling
2. **Test Endpoints**: Verify all protected endpoints work with JWT
3. **User Experience**: Implement token refresh for seamless experience
4. **Monitoring**: Set up alerts for authentication failures
5. **Documentation**: Update API documentation with JWT examples
