# Docker Deployment Guide

## Overview

This guide explains how to deploy the ToDo application using Docker with PostgreSQL database and production configuration.

## Docker Files

- `Dockerfile` - Multi-stage build for production deployment
- `docker-compose.yml` - Development/testing setup with PostgreSQL
- `docker-compose.prod.yml` - Production overrides
- `.dockerignore` - Optimizes build context

## Quick Start

### 1. Development with Docker Compose

```bash
# Clone the repository
git clone <repository-url>
cd ToDoList

# Start the application with PostgreSQL
docker-compose up --build

# Access the application
open http://localhost:8080
```

### 2. Production Deployment

```bash
# Set production environment variables
export DATABASE_PASSWORD=your_secure_password
export JWT_SECRET=your_production_jwt_secret_256_bits
export SECURITY_PASSWORD_PEPPER=your_production_pepper
export SECRET_KEY=your_production_secret

# Start with production configuration
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d
```

## Docker Image Features

### Build Optimizations
- **Multi-stage build** - Smaller final image
- **Dependency caching** - Faster subsequent builds
- **Alpine Linux** - Minimal base image (~100MB)
- **Non-root user** - Enhanced security

### Production Features
- **Health checks** - Automatic health monitoring
- **Memory limits** - JVM optimized for containers
- **Security** - Non-root execution
- **Logging** - Proper log file handling

## Environment Variables

### Required for Production

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://postgres:5432/todoapp
DATABASE_USERNAME=todouser
DATABASE_PASSWORD=your_secure_password

# Security Configuration
JWT_SECRET=your_256_bit_jwt_secret_key
SECURITY_PASSWORD_PEPPER=your_secure_pepper
SECRET_KEY=your_secret_key
```

### Optional Configuration

```bash
# Application Settings
SPRING_PROFILES_ACTIVE=prod
DDL_AUTO=validate
LOG_LEVEL=INFO

# Server Settings
PORT=8080
JAVA_OPTS=-Xmx1g -Xms512m

# Database Pool Settings
DB_POOL_SIZE=20
DB_MIN_IDLE=5
```

## Deployment Scenarios

### 1. Local Development

```bash
# Start with default settings
docker-compose up

# Database: PostgreSQL on port 5432
# Application: http://localhost:8080
# Health Check: http://localhost:8080/actuator/health
```

### 2. Testing Environment

```bash
# Use test profile
docker-compose up -e SPRING_PROFILES_ACTIVE=test
```

### 3. Production Environment

```bash
# Use production overrides
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# With custom environment file
docker-compose --env-file .env.production up -d
```

### 4. Cloud Deployment (Single Container)

```bash
# Build the image
docker build -t todoapp:latest .

# Run with external database
docker run -d \
  --name todoapp \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://your-db-host:5432/todoapp \
  -e DATABASE_USERNAME=todouser \
  -e DATABASE_PASSWORD=your_password \
  -e JWT_SECRET=your_jwt_secret \
  todoapp:latest
```

## Database Setup

### PostgreSQL Initialization

The PostgreSQL container automatically:
1. Creates the `todoapp` database
2. Creates the `todouser` with appropriate permissions
3. Runs initialization scripts from `database/postgresql-schema.sql`

### Manual Database Setup

If using external PostgreSQL:

```sql
-- Connect as superuser
CREATE DATABASE todoapp;
CREATE USER todouser WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE todoapp TO todouser;

-- Connect to todoapp database
\c todoapp

-- Run the schema script
\i database/postgresql-schema.sql
```

## Monitoring and Logs

### Health Checks

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check container health
docker ps
docker logs todoapp
```

### Log Files

```bash
# Application logs
docker-compose logs app

# Database logs
docker-compose logs postgres

# Follow logs in real-time
docker-compose logs -f app
```

### Container Stats

```bash
# Monitor resource usage
docker stats

# Container information
docker inspect todoapp
```

## Scaling and Performance

### Horizontal Scaling

```yaml
# docker-compose.yml
services:
  app:
    deploy:
      replicas: 3
    ports:
      - "8080-8082:8080"
```

### Load Balancer (Nginx)

```nginx
upstream todoapp {
    server localhost:8080;
    server localhost:8081;
    server localhost:8082;
}

server {
    listen 80;
    location / {
        proxy_pass http://todoapp;
    }
}
```

## Security Best Practices

### 1. Container Security
- ✅ Non-root user execution
- ✅ Minimal base image (Alpine)
- ✅ No unnecessary packages
- ✅ Read-only filesystem where possible

### 2. Network Security
- ✅ Internal network for database communication
- ✅ No exposed database ports in production
- ✅ Health check endpoints only

### 3. Secret Management
- ✅ Environment variables for secrets
- ✅ No secrets in Dockerfile or images
- ✅ External secret management integration

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check database container
   docker-compose logs postgres
   
   # Test connection
   docker-compose exec postgres psql -U todouser -d todoapp
   ```

2. **Application Won't Start**
   ```bash
   # Check application logs
   docker-compose logs app
   
   # Check environment variables
   docker-compose exec app env | grep -E "(DATABASE|JWT|SPRING)"
   ```

3. **Health Check Failing**
   ```bash
   # Check health endpoint manually
   docker-compose exec app curl http://localhost:8080/actuator/health
   
   # Check if application is listening
   docker-compose exec app netstat -tlnp
   ```

### Debug Mode

```bash
# Enable debug logging
docker-compose up -e LOG_LEVEL=DEBUG -e HIBERNATE_SQL_LOG_LEVEL=DEBUG
```

## Backup and Recovery

### Database Backup

```bash
# Backup database
docker-compose exec postgres pg_dump -U todouser todoapp > backup.sql

# Restore database
docker-compose exec -T postgres psql -U todouser todoapp < backup.sql
```

### Volume Backup

```bash
# Backup data volume
docker run --rm -v todolist_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres-backup.tar.gz /data
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Push Docker Image

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Build Docker image
        run: docker build -t todoapp:${{ github.sha }} .
      
      - name: Run tests
        run: docker run --rm todoapp:${{ github.sha }} mvn test
      
      - name: Push to registry
        run: |
          docker tag todoapp:${{ github.sha }} your-registry/todoapp:latest
          docker push your-registry/todoapp:latest
```
