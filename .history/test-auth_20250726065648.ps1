# Test Authentication Flow
Write-Host "Testing Authentication Flow..." -ForegroundColor Green
Write-Host ""

# Test 1: Basic connectivity
Write-Host "1. Testing basic connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/test" -Method GET
    Write-Host "   ✅ Backend is running: $($response.message)" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Backend is not running or not accessible" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Admin login
Write-Host ""
Write-Host "2. Testing admin login..." -ForegroundColor Yellow
try {
    $loginData = @{
        phoneNumber = "1234567890"
        password = "admin123"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "   ✅ Login successful: $($response.role)" -ForegroundColor Green
    Write-Host "   Token: $($response.token.Substring(0, 20))..." -ForegroundColor Cyan
    $token = $response.token
} catch {
    Write-Host "   ❌ Login failed" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Response: $($_.Exception.Response)" -ForegroundColor Red
    exit 1
}

# Test 3: Test admin endpoint with token
Write-Host ""
Write-Host "3. Testing admin endpoint with token..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/promo-codes" -Method GET -Headers $headers
    Write-Host "   ✅ Admin endpoint accessible: $($response.promoCodes.Count) promo codes found" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Admin endpoint failed" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
}

# Test 4: Test admin endpoint without token
Write-Host ""
Write-Host "4. Testing admin endpoint without token..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/promo-codes" -Method GET
    Write-Host "   ❌ Should have failed without token" -ForegroundColor Red
} catch {
    Write-Host "   ✅ Correctly rejected without token: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Authentication testing completed!" -ForegroundColor Green 