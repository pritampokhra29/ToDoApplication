# Simple TodoList API Testing Script (Compatible with Windows PowerShell 5.1)
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$testCount = 0
$passCount = 0
$failCount = 0

function Write-TestHeader {
    param([string]$title)
    Write-Host ""
    Write-Host "=======================================" -ForegroundColor Cyan
    Write-Host "  $title" -ForegroundColor Cyan  
    Write-Host "=======================================" -ForegroundColor Cyan
}

function Write-TestCase {
    param([string]$name)
    $script:testCount++
    Write-Host ""
    Write-Host "[$script:testCount] $name" -ForegroundColor Yellow
}

function Test-APIEndpoint {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers,
        [string]$Body = $null,
        [string]$Description
    )
    
    try {
        if ($Body) {
            $result = Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers -Body $Body -ContentType "application/json"
        } else {
            $result = Invoke-RestMethod -Uri $Url -Method $Method -Headers $Headers
        }
        
        Write-Host "  PASS: $Description" -ForegroundColor Green
        $script:passCount++
        return $true
    }
    catch {
        $statusCode = "Unknown"
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode
        }
        Write-Host "  FAIL: $Description (Status: $statusCode)" -ForegroundColor Red
        Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
        $script:failCount++
        return $false
    }
}

# Authentication tokens
$johnAuth = "Basic am9objpwYXNzd29yZDEyMw=="
$janeAuth = "Basic amFuZTpwYXNzd29yZDEyMw=="
$adminAuth = "Basic YWRtaW46YWRtaW4xMjM="

Write-Host ""
Write-Host "=============================================" -ForegroundColor Magenta
Write-Host "     TodoList API Simple Test Suite" -ForegroundColor Magenta
Write-Host "     Base URL: $BaseUrl" -ForegroundColor Magenta
Write-Host "=============================================" -ForegroundColor Magenta

# Check server
Write-TestHeader "SERVER CHECK"
Write-TestCase "Server Connectivity"
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/tasks" -Headers @{"Authorization"=$johnAuth} -UseBasicParsing -TimeoutSec 5
    Write-Host "  PASS: Server is running" -ForegroundColor Green
    $script:passCount++
} catch {
    Write-Host "  FAIL: Server not accessible" -ForegroundColor Red
    Write-Host "  Please start the application: .\mvnw.cmd spring-boot:run" -ForegroundColor Red
    exit 1
}

# Basic Operations
Write-TestHeader "BASIC OPERATIONS"

Write-TestCase "Get All Tasks"
Test-APIEndpoint -Method "GET" -Url "$BaseUrl/tasks" -Headers @{"Authorization"=$johnAuth} -Description "Get all tasks for john"

Write-TestCase "Get Specific Task"
Test-APIEndpoint -Method "POST" -Url "$BaseUrl/tasks/get" -Headers @{"Authorization"=$johnAuth} -Body '{"id": 1}' -Description "Get task with ID 1"

Write-TestCase "Create New Task"
$newTask = '{"title": "Test Task", "description": "Created by test script", "category": "Testing", "priority": "MEDIUM", "status": "PENDING"}'
Test-APIEndpoint -Method "POST" -Url "$BaseUrl/tasks" -Headers @{"Authorization"=$johnAuth} -Body $newTask -Description "Create new task"

# Search Operations
Write-TestHeader "SEARCH OPERATIONS"

Write-TestCase "Search Tasks"
Test-APIEndpoint -Method "POST" -Url "$BaseUrl/tasks/search" -Headers @{"Authorization"=$johnAuth} -Body '{"keyword": "project"}' -Description "Search tasks by keyword"

# Filter Operations
Write-TestHeader "FILTER OPERATIONS"

Write-TestCase "Filter by Status - PENDING"
Test-APIEndpoint -Method "POST" -Url "$BaseUrl/tasks/filter/status" -Headers @{"Authorization"=$johnAuth} -Body '{"status": "PENDING"}' -Description "Filter tasks by PENDING status"

Write-TestCase "Filter by Status - COMPLETED"
Test-APIEndpoint -Method "POST" -Url "$BaseUrl/tasks/filter/status" -Headers @{"Authorization"=$johnAuth} -Body '{"status": "COMPLETED"}' -Description "Filter tasks by COMPLETED status"

Write-TestCase "Filter by Priority"
Test-APIEndpoint -Method "POST" -Url "$BaseUrl/tasks/filter/priority" -Headers @{"Authorization"=$johnAuth} -Body '{"priority": "HIGH"}' -Description "Filter tasks by HIGH priority"

# Update Operations
Write-TestHeader "UPDATE OPERATIONS"

Write-TestCase "Update Task Status"
Test-APIEndpoint -Method "POST" -Url "$BaseUrl/tasks/update" -Headers @{"Authorization"=$johnAuth} -Body '{"id": 2, "status": "COMPLETED"}' -Description "Update task status to COMPLETED"

# Pagination
Write-TestHeader "PAGINATION"

Write-TestCase "Paginated Tasks"
Test-APIEndpoint -Method "GET" -Url "$BaseUrl/tasks/paginated?page=0&size=5" -Headers @{"Authorization"=$johnAuth} -Description "Get paginated tasks"

# User Management
Write-TestHeader "USER MANAGEMENT"

Write-TestCase "Auth Status Check"
Test-APIEndpoint -Method "POST" -Url "$BaseUrl/auth/status" -Headers @{"Authorization"=$johnAuth} -Description "Check authentication status"

# Multi-User Testing
Write-TestHeader "MULTI-USER TESTING"

Write-TestCase "Jane's Tasks"
Test-APIEndpoint -Method "GET" -Url "$BaseUrl/tasks" -Headers @{"Authorization"=$janeAuth} -Description "Get tasks for jane"

Write-TestCase "Admin Tasks"
Test-APIEndpoint -Method "GET" -Url "$BaseUrl/tasks" -Headers @{"Authorization"=$adminAuth} -Description "Get tasks for admin"

# Summary
Write-Host ""
Write-Host "=============================================" -ForegroundColor Magenta
Write-Host "              TEST SUMMARY" -ForegroundColor Magenta
Write-Host "=============================================" -ForegroundColor Magenta
Write-Host ""
Write-Host "  Total Tests: $testCount" -ForegroundColor Cyan
Write-Host "  Passed:      $passCount" -ForegroundColor Green
Write-Host "  Failed:      $failCount" -ForegroundColor Red

if ($testCount -gt 0) {
    $successRate = [math]::Round(($passCount / $testCount) * 100, 1)
    Write-Host "  Success:     $successRate%" -ForegroundColor Green
}

Write-Host ""
if ($failCount -eq 0) {
    Write-Host "SUCCESS: All tests passed!" -ForegroundColor Green
} else {
    Write-Host "NOTICE: Some tests failed. Check the API endpoints." -ForegroundColor Yellow
}
Write-Host ""
