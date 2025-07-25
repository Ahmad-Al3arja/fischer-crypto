# Deposit Approval Test Script
# This script tests the deposit approval functionality to help debug the referral earnings issue

$baseUrl = "http://localhost:8080"
$adminToken = ""
$userToken = ""

Write-Host "Deposit Approval Test Script" -ForegroundColor Green
Write-Host "============================" -ForegroundColor Green

# Function to make HTTP requests with detailed error handling
function Test-Endpoint {
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
    
    Write-Host "`nTesting: $TestName" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Cyan
    
    try {
        if ($Method -eq "GET") {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $Body -ErrorAction Stop
        }
        Write-Host "SUCCESS: $TestName" -ForegroundColor Green
        Write-Host "Response: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor White
        return $response
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errorMessage = $_.Exception.Message
        
        Write-Host "FAILED: $TestName" -ForegroundColor Red
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        Write-Host "Error Message: $errorMessage" -ForegroundColor Red
        
        # Try to get response body for more details
        try {
            $responseStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($responseStream)
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response Body: $responseBody" -ForegroundColor Red
        }
        catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
        
        return $null
    }
}

# Test 1: Admin Login
Write-Host "`nStep 1: Admin Login" -ForegroundColor Cyan
$adminLoginBody = @{
    phoneNumber = "1234567890"
    password = "admin123"
} | ConvertTo-Json

$adminResponse = Test-Endpoint -Method "POST" -Url "$baseUrl/api/auth/login" -Body $adminLoginBody -TestName "Admin Login"

if ($adminResponse) {
    $adminToken = $adminResponse.token
    Write-Host "Admin Token obtained successfully" -ForegroundColor Green
} else {
    Write-Host "Failed to get admin token. Exiting..." -ForegroundColor Red
    exit 1
}

# Test 2: Create a test user (if needed)
Write-Host "`nStep 2: Create Test User" -ForegroundColor Cyan
$registerBody = @{
    fullName = "Test User for Deposit"
    username = "testdeposituser"
    phoneNumber = "+1234567892"
    password = "testpass123"
    confirmPassword = "testpass123"
    referralCode = "admin"
} | ConvertTo-Json

$registerResponse = Test-Endpoint -Method "POST" -Url "$baseUrl/api/auth/register" -Body $registerBody -TestName "User Registration"

# Test 3: User Login
Write-Host "`nStep 3: User Login" -ForegroundColor Cyan
$userLoginBody = @{
    phoneNumber = "+1234567892"
    password = "testpass123"
} | ConvertTo-Json

$userResponse = Test-Endpoint -Method "POST" -Url "$baseUrl/api/auth/login" -Body $userLoginBody -TestName "User Login"

if ($userResponse) {
    $userToken = $userResponse.token
    Write-Host "User Token obtained successfully" -ForegroundColor Green
} else {
    Write-Host "Failed to get user token. Exiting..." -ForegroundColor Red
    exit 1
}

# Test 4: Get available plans
Write-Host "`nStep 4: Get Available Plans" -ForegroundColor Cyan
$plansResponse = Test-Endpoint -Method "GET" -Url "$baseUrl/api/plans" -TestName "Get Plans"

if ($plansResponse -and $plansResponse.plans -and $plansResponse.plans.Count -gt 0) {
    $firstPlan = $plansResponse.plans[0]
    $planId = $firstPlan.id
    Write-Host "Using plan ID: $planId" -ForegroundColor Green
} else {
    Write-Host "No plans available. Exiting..." -ForegroundColor Red
    exit 1
}

# Test 5: Create a deposit
Write-Host "`nStep 5: Create Deposit" -ForegroundColor Cyan
$depositBody = @{
    planId = $planId
    amount = 100.00
    promoCode = ""
} | ConvertTo-Json

$depositResponse = Test-Endpoint -Method "POST" -Url "$baseUrl/api/deposits" -Body $depositBody -Token $userToken -TestName "Create Deposit"

if ($depositResponse) {
    Write-Host "Deposit created successfully" -ForegroundColor Green
} else {
    Write-Host "Failed to create deposit. Exiting..." -ForegroundColor Red
    exit 1
}

# Test 6: Get all deposits (admin)
Write-Host "`nStep 6: Get All Deposits (Admin)" -ForegroundColor Cyan
$depositsResponse = Test-Endpoint -Method "GET" -Url "$baseUrl/api/admin/deposits" -Token $adminToken -TestName "Get All Deposits"

if ($depositsResponse -and $depositsResponse.deposits -and $depositsResponse.deposits.Count -gt 0) {
    $pendingDeposit = $depositsResponse.deposits | Where-Object { $_.status -eq "PENDING" } | Select-Object -First 1
    if ($pendingDeposit) {
        $depositId = $pendingDeposit.id
        Write-Host "Found pending deposit ID: $depositId" -ForegroundColor Green
    } else {
        Write-Host "No pending deposits found. Exiting..." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "No deposits found. Exiting..." -ForegroundColor Red
    exit 1
}

# Test 7: Approve the deposit
Write-Host "`nStep 7: Approve Deposit" -ForegroundColor Cyan
$approveResponse = Test-Endpoint -Method "POST" -Url "$baseUrl/api/admin/deposits/$depositId/approve" -Token $adminToken -TestName "Approve Deposit"

if ($approveResponse) {
    Write-Host "Deposit approved successfully!" -ForegroundColor Green
} else {
    Write-Host "Failed to approve deposit. This is where the referral earnings error occurs." -ForegroundColor Red
}

# Test 8: Verify deposit status
Write-Host "`nStep 8: Verify Deposit Status" -ForegroundColor Cyan
$verifyResponse = Test-Endpoint -Method "GET" -Url "$baseUrl/api/admin/deposits" -Token $adminToken -TestName "Verify Deposit Status"

Write-Host "`nTest Complete!" -ForegroundColor Green
Write-Host "If the approval failed, check the application logs for the exact error message." -ForegroundColor Yellow 