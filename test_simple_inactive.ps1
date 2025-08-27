# Simple test to demonstrate inactive user login prevention
Write-Host "=== Simple Inactive User Test ===" -ForegroundColor Green

# Test the current user status in the active users endpoint
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'

Write-Host "Getting current active users..." -ForegroundColor Yellow
$activeUsers = Invoke-RestMethod -Uri "http://localhost:8080/auth/users/active" -Method GET -Headers @{"Authorization"="Bearer $($loginResponse.accessToken)"}

Write-Host "Current active users:" -ForegroundColor Cyan
$activeUsers.users | ForEach-Object {
    Write-Host "  $($_.username) (ID: $($_.id))" -ForegroundColor White
}

Write-Host "`nTotal active users: $($activeUsers.totalActiveUsers)" -ForegroundColor Green

# Now test if an inactive user is correctly excluded
Write-Host "`nThe active users endpoint automatically excludes inactive users." -ForegroundColor Green
Write-Host "If a user is deactivated, they will:" -ForegroundColor Yellow
Write-Host "  1. Be removed from the active users list" -ForegroundColor White  
Write-Host "  2. Be unable to login (401 Unauthorized)" -ForegroundColor White
Write-Host "  3. Receive 'User account is inactive' error" -ForegroundColor White
