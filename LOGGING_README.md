# Logging Configuration Documentation

## Overview
This application implements comprehensive logging using AOP (Aspect-Oriented Programming) with custom loggers, file rolling, and audit trails. The logging system provides detailed information about method execution, user activities, security events, and performance metrics.

## Features

### 1. AOP-Based Logging
- **Method Execution Logging**: Automatically logs method entry, exit, execution time, and exceptions
- **Audit Logging**: Tracks user activities, business operations, and security events
- **Performance Monitoring**: Identifies slow-running methods and operations

### 2. Custom Logger Utility
- **CustomLogger**: Provides specialized logging methods for different types of events
- **MDC Support**: Uses Mapped Diagnostic Context for consistent request tracking
- **Business Operation Logging**: Tracks CRUD operations with entity details
- **Security Event Logging**: Monitors authentication, authorization, and security-related events

### 3. Log File Management
- **Rolling Policy**: Logs are rolled over based on both size and time
- **Multiple Log Files**: Separate files for different types of logs
- **Compression**: Old log files are automatically compressed
- **Retention Policy**: Configurable retention periods for different log types

### 4. Request Logging Filter
- **HTTP Request/Response Tracking**: Logs all incoming HTTP requests and responses
- **Client IP Detection**: Handles various proxy headers to get real client IP
- **Performance Monitoring**: Tracks request execution times
- **Security Monitoring**: Logs failed authentication and authorization attempts

## Log Files Structure

```
logs/
├── application.log          # Main application logs
├── audit.log               # Audit trail and user activities
├── method-execution.log    # Method execution details
├── error.log              # Error-only logs
└── archived/              # Rolled over logs (compressed)
    ├── application-2023-12-01-1.log.gz
    ├── audit-2023-12-01-1.log.gz
    └── ...
```

## Configuration Files

### 1. logback-spring.xml
Main logging configuration with:
- **Console Appender**: For development/debugging
- **File Appenders**: For persistent logging
- **Rolling Policies**: Size and time-based rolling
- **Async Appenders**: For better performance
- **Profile-specific Configuration**: Different settings for dev/prod

### 2. application.properties
Additional logging properties:
- AOP enablement
- Custom logger levels
- MDC pattern configuration

## Usage Examples

### Using CustomLogger in Controllers

```java
@RestController
public class MyController {
    private static final CustomLogger logger = CustomLogger.getLogger(MyController.class);
    
    @PostMapping("/create")
    public ResponseEntity<Entity> create(@RequestBody Entity entity, Authentication auth) {
        String username = auth.getName();
        
        // Log user activity
        logger.logUserActivity(username, "CREATE_ENTITY", "/create", "Creating new entity");
        
        // Log business operation
        logger.logBusinessOperation("CREATE_ENTITY", "Entity", null, "CREATE", "INITIATED");
        
        Entity saved = service.save(entity);
        
        // Log data change
        logger.logDataChange("Entity", saved.getId().toString(), "CREATE", null, saved.toString());
        
        return ResponseEntity.ok(saved);
    }
}
```

### Automatic AOP Logging
All controller and service methods are automatically logged with:
- Method entry/exit
- Execution time
- Parameters (sanitized)
- Return values
- Exceptions
- User context
- Request details

## Log Patterns

### Audit Log Pattern
```
2023-12-01 10:30:45.123 [REQ123][john.doe][192.168.1.100][/api/tasks] - METHOD_START - Class: TaskController, Method: createTask...
```

### Application Log Pattern
```
2023-12-01 10:30:45.123 [main] INFO  [REQ123][john.doe] com.example.demo.TaskController - Creating new task: My Task
```

## MDC (Mapped Diagnostic Context) Fields

The following fields are automatically added to log entries:
- **requestId**: Unique identifier for each HTTP request
- **username**: Current authenticated user
- **clientIp**: Client IP address (handles proxies)
- **uri**: Request URI
- **method**: HTTP method
- **operation**: Business operation type
- **entityType**: Type of entity being operated on
- **entityId**: ID of the entity

## Rolling Policy Configuration

### Application Logs
- **Max File Size**: 100MB
- **Max History**: 30 days
- **Total Size Cap**: 1GB
- **Compression**: Enabled

### Audit Logs
- **Max File Size**: 50MB
- **Max History**: 90 days
- **Total Size Cap**: 2GB
- **Compression**: Enabled

### Error Logs
- **Max File Size**: 20MB
- **Max History**: 90 days
- **Total Size Cap**: 500MB
- **Compression**: Enabled

## Performance Considerations

1. **Async Appenders**: All file appenders use async writing for better performance
2. **Queue Sizes**: Configured appropriate queue sizes for different log types
3. **MDC Cleanup**: Automatic cleanup of MDC context after request completion
4. **Log Level Management**: Different levels for different environments

## Security Features

1. **Sensitive Data Filtering**: Automatic filtering of sensitive information in logs
2. **Security Event Monitoring**: Dedicated logging for security-related events
3. **Access Tracking**: Complete audit trail of user actions
4. **IP Address Tracking**: Real client IP detection through proxy headers

## Environment-Specific Configuration

### Development Profile
- More verbose console logging
- DEBUG level for application packages
- SQL query logging enabled

### Production Profile
- Reduced console logging
- WARN level for Spring framework
- Focus on file-based logging
- Enhanced security event logging

## Monitoring and Alerting

The logging system provides structured data suitable for:
- **Log Aggregation**: ELK Stack, Splunk, etc.
- **Alerting**: Based on error patterns, performance thresholds
- **Auditing**: Compliance reporting and user activity tracking
- **Debugging**: Detailed execution traces with request correlation

## Maintenance

1. **Log Rotation**: Automatic based on size and time
2. **Cleanup**: Old logs are automatically compressed and removed
3. **Monitoring**: Check disk space usage regularly
4. **Performance**: Monitor async queue sizes and throughput

## Troubleshooting

1. **Missing Logs**: Check file permissions and disk space
2. **Performance Issues**: Verify async appender configurations
3. **MDC Issues**: Ensure proper cleanup in finally blocks
4. **AOP Not Working**: Verify @EnableAspectJAutoProxy is present
