# Test Backend Endpoints
Write-Host "Testing Crypto Backend Endpoints..." -ForegroundColor Green
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
    $token = $response.token
} catch {
    Write-Host "   ❌ Login failed" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Get promo codes
Write-Host ""
Write-Host "3. Testing promo codes endpoint..." -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/promo-codes" -Method GET -Headers $headers
    Write-Host "   ✅ Promo codes retrieved: $($response.promoCodes.Count) codes found" -ForegroundColor Green
    
    if ($response.promoCodes.Count -gt 0) {
        $firstCode = $response.promoCodes[0]
        Write-Host "   First code: $($firstCode.code) - Value: $$($firstCode.bonusValue)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "   ❌ Failed to get promo codes" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Create promo code
Write-Host ""
Write-Host "4. Testing create promo code..." -ForegroundColor Yellow
try {
    $createData = @{
        code = "TEST100"
        bonusValue = 100.00
        usageLimit = 50
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/promo-codes" -Method POST -Body $createData -Headers $headers
    Write-Host "   ✅ Promo code created: $($response.code)" -ForegroundColor Green
    $createdId = $response.id
} catch {
    Write-Host "   ❌ Failed to create promo code" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Toggle promo code (if created successfully)
if ($createdId) {
    Write-Host ""
    Write-Host "5. Testing toggle promo code..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/promo-codes/$createdId/toggle" -Method POST -Headers $headers
        Write-Host "   ✅ Promo code toggled successfully" -ForegroundColor Green
    } catch {
        Write-Host "   ❌ Failed to toggle promo code" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Backend testing completed!" -ForegroundColor Green 