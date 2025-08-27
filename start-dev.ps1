#!/usr/bin/env powershell
# =============================================================================
# Local Development Starter Script
# =============================================================================

Write-Host "🚀 ToDoList Application - Local Development" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green

# Check if Maven wrapper exists
if (-Not (Test-Path ".\mvnw.cmd")) {
    Write-Host "❌ Maven wrapper not found. Please run this script from the project root directory." -ForegroundColor Red
    exit 1
}

# Clean and compile
Write-Host "📦 Cleaning and compiling..." -ForegroundColor Yellow
.\mvnw.cmd clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Compilation failed!" -ForegroundColor Red
    exit 1
}

# Set development environment
$env:SPRING_PROFILES_ACTIVE = "dev"

Write-Host "✅ Starting application in development mode..." -ForegroundColor Green
Write-Host "📊 Profile: dev" -ForegroundColor Cyan
Write-Host "🗄️  Database: H2 (in-memory)" -ForegroundColor Cyan
Write-Host "🔐 Security: Development keys (auto-configured)" -ForegroundColor Cyan
Write-Host "🌐 Application URL: http://localhost:8080" -ForegroundColor Cyan
Write-Host "🔧 H2 Console: http://localhost:8080/h2-console" -ForegroundColor Cyan
Write-Host "📚 API Docs: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "" -ForegroundColor White
Write-Host "Press Ctrl+C to stop the application" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Green

# Start the application
.\mvnw.cmd spring-boot:run
