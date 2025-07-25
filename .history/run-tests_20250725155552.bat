@echo off
REM Investment Platform - Postman Collection Test Runner (Batch Version)
REM This script runs the comprehensive test suite using Newman

setlocal enabledelayedexpansion

REM Default values
set "ENVIRONMENT=local"
set "COLLECTION=crypto.json"
set "OUTPUT_DIR=test-results"
set "VERBOSE="

REM Parse command line arguments
:parse_args
if "%~1"=="" goto :main
if /i "%~1"=="-environment" (
    set "ENVIRONMENT=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="-collection" (
    set "COLLECTION=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="-outputdir" (
    set "OUTPUT_DIR=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="-verbose" (
    set "VERBOSE=--verbose"
    shift
    goto :parse_args
)
if /i "%~1"=="-help" (
    goto :show_help
)
shift
goto :parse_args

:show_help
echo Investment Platform - Postman Collection Test Runner
echo.
echo Usage: run-tests.bat [options]
echo.
echo Options:
echo     -environment ^<env^>     Environment to use (local, staging, prod) [default: local]
echo     -collection ^<file^>     Collection file to run [default: crypto.json]
echo     -outputdir ^<dir^>       Output directory for results [default: test-results]
echo     -verbose                Enable verbose output
echo     -help                   Show this help message
echo.
echo Examples:
echo     run-tests.bat                                    # Run with default settings
echo     run-tests.bat -environment staging               # Run against staging
echo     run-tests.bat -verbose -outputdir results       # Verbose output to custom directory
echo.
pause
exit /b 0

:main
echo 🚀 Investment Platform - Postman Collection Test Runner
echo =====================================================
echo.

REM Check if Newman is installed
newman --version >nul 2>&1
if errorlevel 1 (
    echo Newman is not installed. Installing now...
    npm install -g newman
    if errorlevel 1 (
        echo ❌ Failed to install Newman. Please install it manually: npm install -g newman
        pause
        exit /b 1
    )
    echo ✅ Newman installed successfully!
)

REM Check if collection file exists
if not exist "%COLLECTION%" (
    echo ❌ Collection file not found: %COLLECTION%
    echo Please ensure the collection file exists in the current directory.
    pause
    exit /b 1
)

REM Create environment file if it doesn't exist
set "ENV_FILE=environment-%ENVIRONMENT%.json"
if not exist "%ENV_FILE%" (
    echo Creating environment file: %ENV_FILE%
    call :create_environment_file %ENVIRONMENT%
)

REM Create output directory
if not exist "%OUTPUT_DIR%" (
    echo Creating output directory: %OUTPUT_DIR%
    mkdir "%OUTPUT_DIR%"
)

REM Generate timestamp
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "TIMESTAMP=%dt:~0,8%-%dt:~8,6%"

echo Starting test run...
echo Collection: %COLLECTION%
echo Environment: %ENVIRONMENT%
echo Output Directory: %OUTPUT_DIR%
echo Timestamp: %TIMESTAMP%
echo.

REM Run Newman
set "NEWMAN_CMD=newman run %COLLECTION% -e %ENV_FILE% --reporters cli,json,junit --reporter-json-export "%OUTPUT_DIR%\results-%TIMESTAMP%.json" --reporter-junit-export "%OUTPUT_DIR%\results-%TIMESTAMP%.xml" --delay-request 1000 %VERBOSE%"

%NEWMAN_CMD%
set "EXIT_CODE=%errorlevel%"

if %EXIT_CODE% equ 0 (
    echo.
    echo ✅ Test run completed successfully!
) else (
    echo.
    echo ❌ Test run completed with errors (Exit Code: %EXIT_CODE%)
)

REM Generate summary if results file exists
set "RESULTS_FILE=%OUTPUT_DIR%\results-%TIMESTAMP%.json"
if exist "%RESULTS_FILE%" (
    echo.
    echo 📊 Test Summary:
    REM Note: Full JSON parsing would require PowerShell or additional tools
    echo    Results saved to: %RESULTS_FILE%
)

echo.
pause
exit /b %EXIT_CODE%

:create_environment_file
REM Create environment file based on environment type
set "ENV_TYPE=%~1"
set "ENV_FILE=environment-%ENV_TYPE%.json"

if /i "%ENV_TYPE%"=="local" (
    set "BASE_URL=http://localhost:8080"
) else if /i "%ENV_TYPE%"=="staging" (
    set "BASE_URL=https://staging-api.investmentplatform.com"
) else if /i "%ENV_TYPE%"=="prod" (
    set "BASE_URL=https://api.investmentplatform.com"
) else (
    echo Unknown environment: %ENV_TYPE%. Using local.
    set "BASE_URL=http://localhost:8080"
)

echo Creating environment file: %ENV_FILE%
(
echo {
echo   "name": "Investment Platform - %ENV_TYPE%",
echo   "values": [
echo     {"key": "base_url", "value": "%BASE_URL%", "enabled": true},
echo     {"key": "admin_token", "value": "", "enabled": true},
echo     {"key": "user_token", "value": "", "enabled": true},
echo     {"key": "user2_token", "value": "", "enabled": true},
echo     {"key": "admin_user_id", "value": "", "enabled": true},
echo     {"key": "test_user_id", "value": "", "enabled": true},
echo     {"key": "test_user2_id", "value": "", "enabled": true},
echo     {"key": "test_plan_id", "value": "", "enabled": true},
echo     {"key": "test_deposit_id", "value": "", "enabled": true},
echo     {"key": "test_withdrawal_id", "value": "", "enabled": true},
echo     {"key": "test_promo_code_id", "value": "", "enabled": true},
echo     {"key": "test_wallet_change_id", "value": "", "enabled": true},
echo     {"key": "random_phone", "value": "", "enabled": true},
echo     {"key": "random_username", "value": "", "enabled": true},
echo     {"key": "test_start_time", "value": "", "enabled": true},
echo     {"key": "test_results", "value": "{}", "enabled": true}
echo   ]
echo }
) > "%ENV_FILE%"

echo ✅ Created environment file: %ENV_FILE%
goto :eof 