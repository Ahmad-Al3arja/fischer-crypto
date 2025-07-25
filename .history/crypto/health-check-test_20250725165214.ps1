# Health Check Test Script
# This script tests various health check endpoints to help debug the internal server error

$baseUrl = "http://localhost:8080"

Write-Host "Health Check Test Script" -ForegroundColor Green
Write-Host "========================" -ForegroundColor Green

# Function to make HTTP requests with detailed error handling
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Url,
        [string]$TestName = ""
    )
    
    $headers = @{
        "Content-Type" = "application/json"
    }
    
    Write-Host "`nTesting: $TestName" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Cyan
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -ErrorAction Stop
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

# Test 1: Basic ping endpoint
Test-Endpoint -Method "GET" -Url "$baseUrl/api/test/ping" -TestName "Ping Endpoint"

# Test 2: Custom health check endpoint
Test-Endpoint -Method "GET" -Url "$baseUrl/api/test/health" -TestName "Custom Health Check"

# Test 3: Spring Boot Actuator health endpoint
Test-Endpoint -Method "GET" -Url "$baseUrl/actuator/health" -TestName "Spring Boot Actuator Health"

# Test 4: Spring Boot Actuator info endpoint
Test-Endpoint -Method "GET" -Url "$baseUrl/actuator/info" -TestName "Spring Boot Actuator Info"

# Test 5: Test if server is running at all
Test-Endpoint -Method "GET" -Url "$baseUrl/api/plans" -TestName "Plans Endpoint (to test if server is running)"

Write-Host "`nHealth Check Test Complete!" -ForegroundColor Green 