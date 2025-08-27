# =============================================================================
# ToDoList Application - Local Development Guide
# =============================================================================

## 🚀 Quick Start Commands

### Development Mode (Recommended)
```powershell
# With sample data (may show "No data scripts found" warning - this is normal)
$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw.cmd spring-boot:run

# Or use the provided script with sample data
.\start-dev.ps1

# Simple mode without sample data (no warnings)
.\start-dev-simple.ps1
```

**Note**: 
- Development mode includes all required security configurations automatically
- "No data scripts found" warning is harmless and can be ignored
- Use simple mode if you prefer starting with an empty database

### Production Testing Locally
```powershell
# Set environment variables
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DATABASE_URL="jdbc:h2:mem:testdb"
$env:JWT_SECRET="exXzvSZuVoETpxlhWhkKVxa5LWTDRbzFVseMA6vKuWQ="
$env:SECURITY_PASSWORD_PEPPER="w2IW5jgDXyo4YFqQbZccWg=="
$env:SECRET_KEY="L/P0r+IzeIHvd1qBzO3FRFTL+pcuEQ7g/3TWdI0Sewc="

# Run application
.\mvnw.cmd spring-boot:run
```

## 📱 Application URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Main App** | http://localhost:8080 | Application homepage |
| **API Docs** | http://localhost:8080/swagger-ui.html | Interactive API documentation |
| **H2 Console** | http://localhost:8080/h2-console | Database console (dev mode only) |
| **Health Check** | http://localhost:8080/actuator/health | Application health status |

## 🗄️ Database Access (Development Mode)

### H2 Console Settings:
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: *(leave empty)*
- **Driver Class**: `org.h2.Driver`

## 🔧 Environment Profiles

### Development (`dev`)
- ✅ H2 in-memory database
- ✅ Auto-creates sample data
- ✅ Debug logging enabled
- ✅ H2 console accessible
- ✅ SQL queries logged

### Production (`prod`)
- ✅ PostgreSQL database
- ✅ Console logging only
- ✅ Optimized performance
- ✅ Security hardened

## 🛠️ Development Workflow

### 1. First Time Setup
```powershell
# Clone repository
git clone https://github.com/pritampokhra29/ToDoApplication.git
cd ToDoApplication

# Make scripts executable (if needed)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Start development
.\start-dev.ps1
```

### 2. Daily Development
```powershell
# Pull latest changes
git pull origin RELEASE_1.0.0

# Start development server
$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw.cmd spring-boot:run
```

### 3. Testing Changes
```powershell
# Run tests
.\mvnw.cmd test

# Build for production
.\mvnw.cmd clean package -DskipTests
```

## 🔍 Troubleshooting

### Common Issues:

#### Port 8080 in use
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID)
taskkill /PID <process_id> /F
```

#### Maven wrapper issues
```powershell
# Re-download wrapper
.\mvnw.cmd wrapper:wrapper
```

#### H2 Console not accessible
- Ensure profile is set to `dev`
- Check URL: http://localhost:8080/h2-console
- Verify H2 console is enabled in application-dev.properties

## 📚 API Testing

### Sample API Calls:

#### Health Check
```bash
curl http://localhost:8080/actuator/health
```

#### Register User
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

## 🐳 Docker Development

### Using Docker Compose
```powershell
# Start all services
docker-compose up --build

# Run in background
docker-compose up -d --build

# View logs
docker-compose logs -f todolist-api

# Stop services
docker-compose down
```

## 📊 Monitoring & Logs

### Log Files (Development)
- `logs/application.log` - Application logs
- `logs/audit.log` - Audit events
- `logs/error.log` - Error logs
- `logs/method-execution.log` - Performance logs

### Real-time Log Monitoring
```powershell
# Windows PowerShell
Get-Content logs/application.log -Wait -Tail 50
```

## 🔄 Hot Reload Development

### Using Spring Boot DevTools (if enabled)
- Changes to Java files trigger automatic restart
- Static resources reload automatically
- Properties file changes trigger restart

### Manual Restart
```powershell
# Stop application (Ctrl+C)
# Start again
$env:SPRING_PROFILES_ACTIVE="dev"; .\mvnw.cmd spring-boot:run
```
