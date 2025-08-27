# Database Configuration Guide

## Overview

This guide explains how to configure the ToDo application with externalized database configuration for enhanced security and flexibility.

## Supported Databases

- **H2**: In-memory database for development and testing
- **PostgreSQL**: Production-ready database for production environments

## Configuration Profiles

### Development (`dev` profile)
- Uses H2 in-memory database
- Enables H2 console at `/h2-console`
- Shows SQL queries for debugging
- Auto-creates schema and loads sample data

### Production (`prod` profile)
- Uses PostgreSQL database
- All credentials externalized via environment variables
- Optimized connection pooling
- Restricted logging and monitoring

### Test (`test` profile)
- Uses H2 in-memory database
- Minimal logging
- Isolated test environment

## Environment Variables

### Required for Production

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/todoapp
DATABASE_USERNAME=todouser
DATABASE_PASSWORD=your_secure_password

# Security Configuration
JWT_SECRET=your_256_bit_jwt_secret_key
SECURITY_PASSWORD_PEPPER=your_secure_pepper
SECRET_KEY=your_secret_key

# Profile Selection
SPRING_PROFILES_ACTIVE=prod
```

### Optional Configuration

```bash
# Database Pool Settings
DB_POOL_SIZE=20
DB_MIN_IDLE=5
DB_CONNECTION_TIMEOUT=20000

# Logging Levels
LOG_LEVEL=INFO
HIBERNATE_SQL_LOG_LEVEL=WARN

# Server Configuration
PORT=8080
```

## Local Development Setup

### 1. Using H2 (Default)
```bash
# Set development profile
export SPRING_PROFILES_ACTIVE=dev

# Run the application
./mvnw spring-boot:run
```

### 2. Using Local PostgreSQL
```bash
# Install PostgreSQL locally
# Create database and user (see database/postgresql-schema.sql)

# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/todoapp
export DATABASE_USERNAME=todouser
export DATABASE_PASSWORD=your_password
export JWT_SECRET=your_dev_jwt_secret

# Run the application
./mvnw spring-boot:run
```

## Production Deployment

### Render.com Setup

1. **Create PostgreSQL Database**
   - Add PostgreSQL add-on in Render
   - Render provides `DATABASE_URL` automatically

2. **Set Environment Variables in Render**
   ```
   SPRING_PROFILES_ACTIVE=prod
   JWT_SECRET=YourProductionJWTSecretKey256Bits
   SECURITY_PASSWORD_PEPPER=YourProductionPepper
   SECRET_KEY=YourProductionSecretKey
   ```

3. **Deploy Application**
   - Render will use `DATABASE_URL` for PostgreSQL connection
   - Application will auto-create tables on first run

### Other Cloud Providers

For AWS, Azure, or GCP:

1. **Create PostgreSQL Instance**
2. **Set Environment Variables**
3. **Ensure Network Security Groups Allow Connections**
4. **Use SSL Connection in Production**

## Security Best Practices

### 1. Environment Variables
- Never commit secrets to version control
- Use different secrets for each environment
- Rotate secrets regularly

### 2. Database Security
- Use strong passwords
- Restrict database access by IP
- Enable SSL connections in production
- Regular backups

### 3. Connection Pool
- Limit maximum connections
- Set appropriate timeouts
- Monitor connection leaks

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Check database is running
   - Verify connection string
   - Check firewall settings

2. **Authentication Failed**
   - Verify username/password
   - Check user permissions
   - Ensure database exists

3. **Schema Issues**
   - Check DDL_AUTO setting
   - Verify table permissions
   - Check for schema conflicts

### Debugging

Enable detailed logging:
```bash
export HIBERNATE_SQL_LOG_LEVEL=DEBUG
export LOG_LEVEL=DEBUG
```

## Migration from H2 to PostgreSQL

1. **Export H2 Data** (if needed)
2. **Set up PostgreSQL Database**
3. **Update Environment Variables**
4. **Change Profile to `prod`**
5. **Test Connection**
6. **Deploy Application**

## Monitoring

### Health Checks
- Endpoint: `/actuator/health`
- Database connectivity status
- Connection pool metrics

### Logging
- Application logs in `./logs/application.log`
- Database query logs (configurable)
- Connection pool statistics

## Sample Configuration Files

See:
- `.env.template` - Environment variables template
- `database/postgresql-schema.sql` - Database schema
- `application-prod.properties` - Production configuration
- `application-dev.properties` - Development configuration
