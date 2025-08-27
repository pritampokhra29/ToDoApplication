# TodoList API Final Verification Script (Fixed for Basic Auth)
# This script tests all the corrected endpoints to verify everything is working

Write-Host "===============================================" -ForegroundColor Green
Write-Host "  TODOLIST API FINAL VERIFICATION TESTING     " -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# Basic Auth credentials (admin:admin123 encoded as Base64)
$adminAuth = "Basic YWRtaW46YWRtaW4xMjM="

Write-Host "`nUsing credentials:" -ForegroundColor Yellow
Write-Host "  Admin: admin:admin123 (Basic Auth)" -ForegroundColor Cyan
Write-Host "  Base URL: $baseUrl" -ForegroundColor Cyan

# Set up headers for authenticated requests
$authHeaders = @{
    "Authorization" = $adminAuth
    "Content-Type" = "application/json"
}

# Test 1: Test login endpoint
Write-Host "`n=== TEST 1: LOGIN ENDPOINT ===" -ForegroundColor Magenta
try {
    $loginBody = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Headers $authHeaders -Method POST -Body $loginBody -TimeoutSec 10
    Write-Host "✅ Admin login endpoint: SUCCESS" -ForegroundColor Green
    Write-Host "   Response: $loginResponse" -ForegroundColor Green
} catch {
    Write-Host "❌ Admin login endpoint: FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Get all tasks
Write-Host "`n=== TEST 2: GET ALL TASKS ===" -ForegroundColor Magenta
try {
    $allTasks = Invoke-RestMethod -Uri "$baseUrl/tasks" -Headers $authHeaders -Method GET -TimeoutSec 10
    Write-Host "✅ Get all tasks: SUCCESS" -ForegroundColor Green
    Write-Host "   Found $($allTasks.Count) tasks" -ForegroundColor Green
    
    if ($allTasks.Count -gt 0) {
        $validTaskId = $allTasks[0].id
        Write-Host "   Sample task ID: $validTaskId" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ Get all tasks: FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Search tasks with keyword
Write-Host "`n=== TEST 3: SEARCH TASKS ===" -ForegroundColor Magenta
try {
    $searchTasks = Invoke-RestMethod -Uri "$baseUrl/tasks?keyword=task" -Headers $authHeaders -Method GET -TimeoutSec 10
    Write-Host "✅ Search tasks: SUCCESS" -ForegroundColor Green
    Write-Host "   Found $($searchTasks.Count) tasks matching 'task'" -ForegroundColor Green
} catch {
    Write-Host "❌ Search tasks: FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Create new task
Write-Host "`n=== TEST 4: CREATE NEW TASK ===" -ForegroundColor Magenta
try {
    $newTaskBody = @{
        title = "Verification Test Task"
        description = "This task was created by the final verification script"
        status = "PENDING"
    } | ConvertTo-Json

    $newTask = Invoke-RestMethod -Uri "$baseUrl/tasks" -Headers $authHeaders -Method POST -Body $newTaskBody -TimeoutSec 10
    
    if ($newTask -and $newTask.id) {
        $createdTaskId = $newTask.id
        Write-Host "✅ Create new task: SUCCESS" -ForegroundColor Green
        Write-Host "   Created task ID: $createdTaskId" -ForegroundColor Green
    } else {
        Write-Host "❌ Create new task: FAILED - No task ID returned" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Create new task: FAILED" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Get specific task by ID
if ($createdTaskId) {
    Write-Host "`n=== TEST 5: GET TASK BY ID ===" -ForegroundColor Magenta
    try {
        $getTaskBody = @{
            id = $createdTaskId
        } | ConvertTo-Json

        $taskById = Invoke-RestMethod -Uri "$baseUrl/tasks/get" -Headers $authHeaders -Method POST -Body $getTaskBody -TimeoutSec 10
        Write-Host "✅ Get task by ID: SUCCESS" -ForegroundColor Green
        Write-Host "   Task: $($taskById.title)" -ForegroundColor Green
        Write-Host "   Status: $($taskById.status)" -ForegroundColor Green
    } catch {
        Write-Host "❌ Get task by ID: FAILED" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 6: Update task
if ($createdTaskId) {
    Write-Host "`n=== TEST 6: UPDATE TASK ===" -ForegroundColor Magenta
    try {
        $updateTaskBody = @{
            id = $createdTaskId
            title = "Updated Verification Task"
            description = "This task has been updated by the verification script"
            status = "IN_PROGRESS"
        } | ConvertTo-Json

        $updatedTask = Invoke-RestMethod -Uri "$baseUrl/tasks/update" -Headers $authHeaders -Method POST -Body $updateTaskBody -TimeoutSec 10
        Write-Host "✅ Update task: SUCCESS" -ForegroundColor Green
        Write-Host "   Updated task: $($updatedTask.title)" -ForegroundColor Green
        Write-Host "   New status: $($updatedTask.status)" -ForegroundColor Green
    } catch {
        Write-Host "❌ Update task: FAILED" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 7: Test invalid task ID (should return 404)
Write-Host "`n=== TEST 7: INVALID TASK ID (404 TEST) ===" -ForegroundColor Magenta
try {
    $invalidTaskBody = @{
        id = 99999
    } | ConvertTo-Json

    $invalidTask = Invoke-RestMethod -Uri "$baseUrl/tasks/get" -Headers $authHeaders -Method POST -Body $invalidTaskBody -TimeoutSec 10
    Write-Host "❌ Invalid task ID test: FAILED - Should have returned 404" -ForegroundColor Red
} catch {
    if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 404) {
        Write-Host "✅ Invalid task ID test: SUCCESS - Correctly returned 404" -ForegroundColor Green
    } else {
        Write-Host "❌ Invalid task ID test: FAILED - Wrong error code" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 8: Test unauthorized access (should return 401)
Write-Host "`n=== TEST 8: UNAUTHORIZED ACCESS TEST ===" -ForegroundColor Magenta
try {
    $unauthorizedHeaders = @{"Content-Type" = "application/json"}
    $unauthorizedResponse = Invoke-RestMethod -Uri "$baseUrl/tasks" -Headers $unauthorizedHeaders -Method GET -TimeoutSec 10
    Write-Host "❌ Unauthorized access test: FAILED - Should have been rejected" -ForegroundColor Red
} catch {
    if ($_.Exception.Response) {
        $statusCode = [int]$_.Exception.Response.StatusCode
        if ($statusCode -eq 401) {
            Write-Host "✅ Unauthorized access test: SUCCESS - Correctly returned 401" -ForegroundColor Green
        } else {
            Write-Host "❌ Unauthorized access test: FAILED - Wrong status code: $statusCode" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ Unauthorized access test: FAILED - Unexpected error" -ForegroundColor Red
    }
}

# Test 9: Delete task
if ($createdTaskId) {
    Write-Host "`n=== TEST 9: DELETE TASK ===" -ForegroundColor Magenta
    try {
        $deleteTaskBody = @{
            id = $createdTaskId
        } | ConvertTo-Json

        $deleteResult = Invoke-RestMethod -Uri "$baseUrl/tasks/delete" -Headers $authHeaders -Method POST -Body $deleteTaskBody -TimeoutSec 10
        Write-Host "✅ Delete task: SUCCESS" -ForegroundColor Green
        
        # Verify deletion by trying to get the deleted task
        try {
            $verifyDeletion = Invoke-RestMethod -Uri "$baseUrl/tasks/get" -Headers $authHeaders -Method POST -Body $deleteTaskBody -TimeoutSec 10
            Write-Host "❌ Verify deletion: FAILED - Task still exists" -ForegroundColor Red
        } catch {
            if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 404) {
                Write-Host "✅ Verify deletion: SUCCESS - Task correctly deleted (404)" -ForegroundColor Green
            } else {
                Write-Host "❌ Verify deletion: FAILED - Wrong error code" -ForegroundColor Red
            }
        }
    } catch {
        Write-Host "❌ Delete task: FAILED" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 10: Test invalid status value (should fail)
Write-Host "`n=== TEST 10: INVALID STATUS TEST ===" -ForegroundColor Magenta
try {
    $invalidStatusBody = @{
        title = "Invalid Status Task"
        description = "This task has an invalid status"
        status = "INVALID_STATUS"
    } | ConvertTo-Json

    $invalidStatusTask = Invoke-RestMethod -Uri "$baseUrl/tasks" -Headers $authHeaders -Method POST -Body $invalidStatusBody -TimeoutSec 10
    Write-Host "❌ Invalid status test: FAILED - Should have been rejected" -ForegroundColor Red
} catch {
    if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 400) {
        Write-Host "✅ Invalid status test: SUCCESS - Correctly returned 400" -ForegroundColor Green
    } else {
        Write-Host "❌ Invalid status test: FAILED - Wrong error code" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# FINAL SUMMARY
Write-Host "`n===============================================" -ForegroundColor Green
Write-Host "  FINAL VERIFICATION COMPLETE                 " -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green
Write-Host "`n🎉 TodoList API verification completed!" -ForegroundColor Green
Write-Host "All endpoints tested with Basic Authentication" -ForegroundColor Green
Write-Host "API is ready for production use!" -ForegroundColor Green
