# Crypto Investment Platform - Complete API Test Script
# Run this script to test all endpoints quickly

$baseUrl = "http://localhost:8080"
$adminToken = ""
$userToken = ""

Write-Host "Starting Comprehensive API Test Suite..." -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green

# Function to make HTTP requests
function Invoke-TestRequest {
    param(
        [string]$Method,
        [string]$Url,
        [string]$Body = "",
        [string]$Token = "",
        [string]$TestName = ""
    )
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    if ($Token) {
        $headers["Authorization"] = "Bearer $Token"
    }
    
    try {
        if ($Method -eq "GET") {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $Body -ErrorAction Stop
        }
        Write-Host "SUCCESS: $TestName" -ForegroundColor Green
        return $response
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorMessage = $_.Exception.Message
        Write-Host "FAILED: $TestName ($statusCode): $errorMessage" -ForegroundColor Red
        return $null
    }
}

# Test 1: Health Check
Write-Host "`nTesting Health Check..." -ForegroundColor Yellow
$health = Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/test/health" -TestName "Health Check"

# Test 2: Get All Plans (Public)
Write-Host "`nTesting Public Endpoints..." -ForegroundColor Yellow
$plans = Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/plans" -TestName "Get All Plans"

if ($plans) {
    $firstPlanId = $plans.plans[0].id
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/plans/$firstPlanId" -TestName "Get Plan by ID"
}

# Test 3: Authentication
Write-Host "`nTesting Authentication..." -ForegroundColor Yellow

# Test Admin Login
$adminLoginBody = @{
    phoneNumber = "1234567890"
    password = "admin123"
} | ConvertTo-Json

$adminResponse = Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/auth/login" -Body $adminLoginBody -TestName "Admin Login"

if ($adminResponse) {
    $adminToken = $adminResponse.token
    Write-Host "Admin Token obtained" -ForegroundColor Cyan
}

# Test User Registration
$registerBody = @{
    fullName = "Test User"
    username = "testuser"
    phoneNumber = "+1234567891"
    password = "testpass123"
    confirmPassword = "testpass123"
    referralCode = "admin"
} | ConvertTo-Json

$registerResponse = Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/auth/register" -Body $registerBody -TestName "User Registration"

# Test User Login
$userLoginBody = @{
    phoneNumber = "+1234567891"
    password = "testpass123"
} | ConvertTo-Json

$userResponse = Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/auth/login" -Body $userLoginBody -TestName "User Login"

if ($userResponse) {
    $userToken = $userResponse.token
    Write-Host "User Token obtained" -ForegroundColor Cyan
}

# Test 4: User Endpoints (with user token)
if ($userToken) {
    Write-Host "`nTesting User Endpoints..." -ForegroundColor Yellow
    
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/user/dashboard" -Token $userToken -TestName "Get User Dashboard"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/user/profile" -Token $userToken -TestName "Get User Profile"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/user/referral-stats" -Token $userToken -TestName "Get Referral Stats"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/transactions/balance" -Token $userToken -TestName "Get User Balance"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/transactions/wallet" -Token $userToken -TestName "Get Wallet Info"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/transactions/deposit-info" -Token $userToken -TestName "Get Deposit Info"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/transactions/deposit-history" -Token $userToken -TestName "Get Deposit History"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/transactions/withdrawal-history" -Token $userToken -TestName "Get Withdrawal History"
    
    # Test Wallet Save
    $walletBody = @{
        usdtAddress = "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE"
    } | ConvertTo-Json
    
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/transactions/wallet/save" -Token $userToken -Body $walletBody -TestName "Save Wallet Address"
    
    # Test Deposit Creation
    $depositBody = @{
        amount = 100.00
        planId = 1
        promoCode = "WELCOME10"
    } | ConvertTo-Json
    
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/transactions/deposit" -Token $userToken -Body $depositBody -TestName "Create Deposit"
    
    # Test Withdrawal Creation
    $withdrawalBody = @{
        amount = 50.00
        walletAddress = "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE"
    } | ConvertTo-Json
    
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/transactions/withdraw" -Token $userToken -Body $withdrawalBody -TestName "Create Withdrawal"
    
    # Test Counter Operations
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/user/activate-counter" -Token $userToken -TestName "Activate Counter"
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/user/complete-counter" -Token $userToken -TestName "Complete Counter"
}

# Test 5: Admin Endpoints (with admin token)
if ($adminToken) {
    Write-Host "`nTesting Admin Endpoints..." -ForegroundColor Yellow
    
    # User Management
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/admin/users" -Token $adminToken -TestName "Get All Users"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/admin/users/1" -Token $adminToken -TestName "Get User Details"
    
    # Balance Update
    $balanceBody = @{
        amount = 100.00
        reason = "Test bonus"
    } | ConvertTo-Json
    
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/admin/users/1/balance" -Token $adminToken -Body $balanceBody -TestName "Update User Balance"
    
    # Deposit Management
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/admin/deposits" -Token $adminToken -TestName "Get All Deposits"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/admin/deposits?status=PENDING" -Token $adminToken -TestName "Get Pending Deposits"
    
    # Withdrawal Management
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/admin/withdrawals" -Token $adminToken -TestName "Get All Withdrawals"
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/admin/withdrawals?status=PENDING" -Token $adminToken -TestName "Get Pending Withdrawals"
    
    # Promo Code Management
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/admin/promo-codes" -Token $adminToken -TestName "Get All Promo Codes"
    
    $promoBody = @{
        code = "TESTPROMO"
        bonusValue = 25.00
        usageLimit = 50
        expiresAt = "2024-12-31T23:59:59"
    } | ConvertTo-Json
    
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/admin/promo-codes" -Token $adminToken -Body $promoBody -TestName "Create Promo Code"
    
    # Plan Management
    $createPlanBody = @{
        name = "Test Plan"
        price = 750.00
        monthlyProfit = 90.00
        dailyProfitMin = 2.70
        dailyProfitMax = 3.30
        planLevel = 6
    } | ConvertTo-Json
    
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/plans" -Token $adminToken -Body $createPlanBody -TestName "Create New Plan"
    
    $updatePlanBody = @{
        name = "Updated Test Plan"
        price = 800.00
        monthlyProfit = 100.00
    } | ConvertTo-Json
    
    Invoke-TestRequest -Method "PUT" -Url "$baseUrl/api/plans/6" -Token $adminToken -Body $updatePlanBody -TestName "Update Plan"
    
    # Admin Settings
    Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/admin/settings" -Token $adminToken -TestName "Get Admin Settings"
    
    $maintenanceBody = @{
        enabled = $true
    } | ConvertTo-Json
    
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/admin/settings/maintenance" -Token $adminToken -Body $maintenanceBody -TestName "Toggle Maintenance Mode"
    
    $aboutBody = @{
        content = "Updated about content for testing"
    } | ConvertTo-Json
    
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/admin/settings/about" -Token $adminToken -Body $aboutBody -TestName "Update About Content"
    
    # Counter Management
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/admin/users/1/counter/activate" -Token $adminToken -TestName "Activate User Counter"
    Invoke-TestRequest -Method "POST" -Url "$baseUrl/api/admin/users/1/counter/deactivate" -Token $adminToken -TestName "Deactivate User Counter"
}

# Test 6: Test Endpoints
Write-Host "`nTesting Debug Endpoints..." -ForegroundColor Yellow
Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/test/users" -TestName "Get All Users (Test)"
Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/test/login-test" -TestName "Login Test Info"
Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/auth/test-admin" -TestName "Test Admin User"
Invoke-TestRequest -Method "GET" -Url "$baseUrl/api/auth/test-referral/admin" -TestName "Test Referral Code"

Write-Host "`nAPI Test Suite Completed!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host "Check the results above for any failed endpoints." -ForegroundColor Cyan
Write-Host "Green = Success, Red = Failed" -ForegroundColor Cyan 