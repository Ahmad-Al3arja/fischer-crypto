# Investment Platform - Postman Collection Test Runner
# This script runs the comprehensive test suite using Newman

param(
    [string]$Environment = "local",
    [string]$Collection = "crypto.json",
    [string]$OutputDir = "test-results",
    [switch]$Verbose,
    [switch]$Help
)

# Show help if requested
if ($Help) {
    Write-Host @"
Investment Platform - Postman Collection Test Runner

Usage: .\run-tests.ps1 [options]

Options:
    -Environment <env>     Environment to use (local, staging, prod) [default: local]
    -Collection <file>     Collection file to run [default: crypto.json]
    -OutputDir <dir>       Output directory for results [default: test-results]
    -Verbose              Enable verbose output
    -Help                 Show this help message

Examples:
    .\run-tests.ps1                                    # Run with default settings
    .\run-tests.ps1 -Environment staging               # Run against staging
    .\run-tests.ps1 -Verbose -OutputDir results       # Verbose output to custom directory

"@
    exit 0
}

# Function to check if Newman is installed
function Test-Newman {
    try {
        $null = Get-Command newman -ErrorAction Stop
        return $true
    }
    catch {
        return $false
    }
}

# Function to install Newman
function Install-Newman {
    Write-Host "Installing Newman..." -ForegroundColor Yellow
    try {
        npm install -g newman
        Write-Host "Newman installed successfully!" -ForegroundColor Green
    }
    catch {
        Write-Host "Failed to install Newman. Please install it manually: npm install -g newman" -ForegroundColor Red
        exit 1
    }
}

# Function to create environment file
function New-EnvironmentFile {
    param([string]$Environment)
    
    $envFile = "environment-$Environment.json"
    
    switch ($Environment.ToLower()) {
        "local" {
            $envContent = @{
                name = "Investment Platform - Local"
                values = @(
                    @{ key = "base_url"; value = "http://localhost:8080"; enabled = $true }
                    @{ key = "admin_token"; value = ""; enabled = $true }
                    @{ key = "user_token"; value = ""; enabled = $true }
                    @{ key = "user2_token"; value = ""; enabled = $true }
                    @{ key = "admin_user_id"; value = ""; enabled = $true }
                    @{ key = "test_user_id"; value = ""; enabled = $true }
                    @{ key = "test_user2_id"; value = ""; enabled = $true }
                    @{ key = "test_plan_id"; value = ""; enabled = $true }
                    @{ key = "test_deposit_id"; value = ""; enabled = $true }
                    @{ key = "test_withdrawal_id"; value = ""; enabled = $true }
                    @{ key = "test_promo_code_id"; value = ""; enabled = $true }
                    @{ key = "test_wallet_change_id"; value = ""; enabled = $true }
                    @{ key = "random_phone"; value = ""; enabled = $true }
                    @{ key = "random_username"; value = ""; enabled = $true }
                    @{ key = "test_start_time"; value = ""; enabled = $true }
                    @{ key = "test_results"; value = "{}"; enabled = $true }
                )
            }
        }
        "staging" {
            $envContent = @{
                name = "Investment Platform - Staging"
                values = @(
                    @{ key = "base_url"; value = "https://staging-api.investmentplatform.com"; enabled = $true }
                    @{ key = "admin_token"; value = ""; enabled = $true }
                    @{ key = "user_token"; value = ""; enabled = $true }
                    @{ key = "user2_token"; value = ""; enabled = $true }
                    @{ key = "admin_user_id"; value = ""; enabled = $true }
                    @{ key = "test_user_id"; value = ""; enabled = $true }
                    @{ key = "test_user2_id"; value = ""; enabled = $true }
                    @{ key = "test_plan_id"; value = ""; enabled = $true }
                    @{ key = "test_deposit_id"; value = ""; enabled = $true }
                    @{ key = "test_withdrawal_id"; value = ""; enabled = $true }
                    @{ key = "test_promo_code_id"; value = ""; enabled = $true }
                    @{ key = "test_wallet_change_id"; value = ""; enabled = $true }
                    @{ key = "random_phone"; value = ""; enabled = $true }
                    @{ key = "random_username"; value = ""; enabled = $true }
                    @{ key = "test_start_time"; value = ""; enabled = $true }
                    @{ key = "test_results"; value = "{}"; enabled = $true }
                )
            }
        }
        "prod" {
            $envContent = @{
                name = "Investment Platform - Production"
                values = @(
                    @{ key = "base_url"; value = "https://api.investmentplatform.com"; enabled = $true }
                    @{ key = "admin_token"; value = ""; enabled = $true }
                    @{ key = "user_token"; value = ""; enabled = $true }
                    @{ key = "user2_token"; value = ""; enabled = $true }
                    @{ key = "admin_user_id"; value = ""; enabled = $true }
                    @{ key = "test_user_id"; value = ""; enabled = $true }
                    @{ key = "test_user2_id"; value = ""; enabled = $true }
                    @{ key = "test_plan_id"; value = ""; enabled = $true }
                    @{ key = "test_deposit_id"; value = ""; enabled = $true }
                    @{ key = "test_withdrawal_id"; value = ""; enabled = $true }
                    @{ key = "test_promo_code_id"; value = ""; enabled = $true }
                    @{ key = "test_wallet_change_id"; value = ""; enabled = $true }
                    @{ key = "random_phone"; value = ""; enabled = $true }
                    @{ key = "random_username"; value = ""; enabled = $true }
                    @{ key = "test_start_time"; value = ""; enabled = $true }
                    @{ key = "test_results"; value = "{}"; enabled = $true }
                )
            }
        }
        default {
            Write-Host "Unknown environment: $Environment. Using local." -ForegroundColor Yellow
            New-EnvironmentFile "local"
            return "environment-local.json"
        }
    }
    
    $envContent | ConvertTo-Json -Depth 10 | Out-File -FilePath $envFile -Encoding UTF8
    Write-Host "Created environment file: $envFile" -ForegroundColor Green
    return $envFile
}

# Function to create output directory
function New-OutputDirectory {
    param([string]$OutputDir)
    
    if (!(Test-Path $OutputDir)) {
        New-Item -ItemType Directory -Path $OutputDir | Out-Null
        Write-Host "Created output directory: $OutputDir" -ForegroundColor Green
    }
}

# Function to run tests
function Start-TestRun {
    param(
        [string]$Collection,
        [string]$Environment,
        [string]$OutputDir,
        [bool]$Verbose
    )
    
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $envFile = "environment-$Environment.json"
    
    # Check if environment file exists, create if not
    if (!(Test-Path $envFile)) {
        $envFile = New-EnvironmentFile $Environment
    }
    
    # Create output directory
    New-OutputDirectory $OutputDir
    
    # Build Newman command
    $newmanCmd = "newman run $Collection -e $envFile"
    
    # Add reporters
    $newmanCmd += " --reporters cli,json,junit"
    $newmanCmd += " --reporter-json-export `"$OutputDir\results-$timestamp.json`""
    $newmanCmd += " --reporter-junit-export `"$OutputDir\results-$timestamp.xml`""
    
    # Add verbose flag if requested
    if ($Verbose) {
        $newmanCmd += " --verbose"
    }
    
    # Add delay between requests
    $newmanCmd += " --delay-request 1000"
    
    Write-Host "Starting test run..." -ForegroundColor Cyan
    Write-Host "Collection: $Collection" -ForegroundColor White
    Write-Host "Environment: $Environment" -ForegroundColor White
    Write-Host "Output Directory: $OutputDir" -ForegroundColor White
    Write-Host "Timestamp: $timestamp" -ForegroundColor White
    Write-Host ""
    
    # Run Newman
    try {
        Invoke-Expression $newmanCmd
        $exitCode = $LASTEXITCODE
        
        if ($exitCode -eq 0) {
            Write-Host ""
            Write-Host "✅ Test run completed successfully!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "❌ Test run completed with errors (Exit Code: $exitCode)" -ForegroundColor Red
        }
        
        # Generate summary
        $jsonFile = "$OutputDir\results-$timestamp.json"
        if (Test-Path $jsonFile) {
            $results = Get-Content $jsonFile | ConvertFrom-Json
            Write-Host ""
            Write-Host "📊 Test Summary:" -ForegroundColor Cyan
            Write-Host "   Total Tests: $($results.run.stats.assertions.total)" -ForegroundColor White
            Write-Host "   Passed: $($results.run.stats.assertions.passed)" -ForegroundColor Green
            Write-Host "   Failed: $($results.run.stats.assertions.failed)" -ForegroundColor Red
            Write-Host "   Skipped: $($results.run.stats.assertions.skipped)" -ForegroundColor Yellow
            Write-Host "   Duration: $($results.run.timings.completed - $results.run.timings.started)ms" -ForegroundColor White
        }
        
        return $exitCode
    }
    catch {
        Write-Host "❌ Error running tests: $($_.Exception.Message)" -ForegroundColor Red
        return 1
    }
}

# Main execution
Write-Host "🚀 Investment Platform - Postman Collection Test Runner" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# Check if Newman is installed
if (!(Test-Newman)) {
    Write-Host "Newman is not installed. Installing now..." -ForegroundColor Yellow
    Install-Newman
}

# Check if collection file exists
if (!(Test-Path $Collection)) {
    Write-Host "❌ Collection file not found: $Collection" -ForegroundColor Red
    Write-Host "Please ensure the collection file exists in the current directory." -ForegroundColor Yellow
    exit 1
}

# Run tests
$exitCode = Start-TestRun -Collection $Collection -Environment $Environment -OutputDir $OutputDir -Verbose $Verbose

# Exit with appropriate code
exit $exitCode 