# Email Notification Feature

This document describes the email notification feature that sends reminders for tasks due tomorrow.

## Overview

The email notification system automatically checks for tasks due tomorrow and sends email reminders to users at 9:00 AM daily. This helps users stay on top of their upcoming deadlines.

## Configuration

### Email Service Setup

The application uses Spring Boot Mail with Gmail SMTP as the free mailing service. To configure email notifications:

1. **Set up Gmail App Password** (if using Gmail):
   - Enable 2-factor authentication on your Gmail account
   - Generate an App Password: Google Account Settings > Security > App passwords
   - Use this app password instead of your regular Gmail password

2. **Configure Environment Variables**:
   ```bash
   export MAIL_USERNAME=your-email@gmail.com
   export MAIL_PASSWORD=your-app-password
   export MAIL_FROM=todoapp@gmail.com
   ```

3. **Application Properties** (already configured):
   - Email notifications are enabled by default
   - Uses Gmail SMTP with TLS encryption
   - Configurable through environment variables

### Configuration Properties

The following properties can be customized in `application.properties`:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}

# Notification Settings
notification.email.enabled=true
notification.email.from=${MAIL_FROM:todoapp@gmail.com}
notification.email.subject.prefix=[ToDoApp]
```

## Features

### Automatic Notifications

- **Schedule**: Daily at 9:00 AM
- **Scope**: All tasks due tomorrow (not deleted)
- **Recipients**: Task owners with valid email addresses
- **Content**: Task title, due date, and reminder message

### Manual Triggering

For testing purposes, administrators can manually trigger the notification check:

```bash
POST /api/notifications/check-due-tasks
Authorization: Bearer <admin-jwt-token>
```

### Email Template

The notification email includes:
- Subject: `[ToDoApp] Task Due Tomorrow: <Task Title>`
- Task details (title and due date)
- Professional reminder message

## Technical Implementation

### Components

1. **EmailService**: Handles email sending using Spring Mail
2. **NotificationService**: Manages scheduled task checking and notification logic
3. **NotificationController**: Provides manual trigger endpoint for testing
4. **Scheduled Job**: Runs daily using Spring's @Scheduled annotation

### Database Dependencies

- Requires `User.email` field to be populated
- Uses `Task.dueDate` for comparison
- Respects `Task.deleted` flag (no notifications for deleted tasks)

### Error Handling

- Graceful handling of email sending failures
- Comprehensive logging for monitoring and debugging
- Skips tasks without valid user email addresses

## Testing

### Unit Tests

Run the notification service tests:
```bash
mvn test -Dtest=NotificationServiceTest
```

### Integration Testing

1. Create test tasks due tomorrow in the database
2. Ensure user has valid email address
3. Use manual trigger endpoint to test notification
4. Check logs for successful email sending

### Monitoring

Check application logs for:
- Daily scheduled runs: "Starting scheduled check for tasks due tomorrow"
- Email sending success: "Notification sent for task..."
- Error conditions: "Failed to send notification..."

## Alternative Email Services

While the current implementation uses Gmail SMTP, it can be easily configured for other free services:

### SendGrid (Free Tier)
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=your-sendgrid-api-key
```

### Mailgun (Free Tier)
```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=your-mailgun-username
spring.mail.password=your-mailgun-password
```

## Security Considerations

- Email credentials stored as environment variables (not in code)
- Admin-only access to manual trigger endpoint
- TLS encryption for SMTP communication
- Rate limiting through scheduling (once daily)

## Limitations

- Requires internet connectivity for SMTP server access
- Depends on external email service availability
- Email delivery not guaranteed (depends on recipient server)
- Single daily notification (no multiple reminders)