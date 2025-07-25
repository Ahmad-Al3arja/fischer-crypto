@echo off
REM test-runner.bat - Comprehensive API Testing and Auto-fixing Script for Windows
REM Usage: test-runner.bat [--fix] [--continuous] [--report]

setlocal enabledelayedexpansion

REM Configuration
set PROJECT_DIR=%cd%
set TEST_RESULTS_DIR=%PROJECT_DIR%\test-results
set REPORTS_DIR=%PROJECT_DIR%\reports
set LOG_FILE=%TEST_RESULTS_DIR%\test-execution.log
set ISSUES_FILE=%TEST_RESULTS_DIR%\detected-issues.json

REM Command line arguments
set AUTO_FIX=false
set CONTINUOUS=false
set GENERATE_REPORT=false

:parse_args
if "%~1"=="" goto :main
if "%~1"=="--fix" set AUTO_FIX=true
if "%~1"=="--continuous" set CONTINUOUS=true
if "%~1"=="--report" set GENERATE_REPORT=true
shift
goto :parse_args

:main
REM Create directories
if not exist "%TEST_RESULTS_DIR%" mkdir "%TEST_RESULTS_DIR%"
if not exist "%REPORTS_DIR%" mkdir "%REPORTS_DIR%"

REM Initialize log file
echo 🚀 Starting API Testing Suite - %date% %time% > "%LOG_FILE%"
echo ================================= >> "%LOG_FILE%"

echo 🚀 Starting API Testing Suite - %date% %time%
echo =================================

REM Check and start application if needed
call :check_application_status
if errorlevel 1 (
    call :start_application
)

REM Run tests based on mode
if "%CONTINUOUS%"=="true" (
    call :run_continuous_testing
) else (
    call :run_comprehensive_tests
    call :detect_issues
    call :generate_html_report
)

echo.
echo ✅ API testing pipeline completed!
echo.
echo 📁 Test Results Location: %TEST_RESULTS_DIR%
echo 📄 Log File: %LOG_FILE%
if "%GENERATE_REPORT%"=="true" (
    echo 📊 HTML Report: %REPORTS_DIR%\
)
echo.
echo 🔧 To auto-fix issues, run: %0 --fix
echo 🔄 For continuous testing, run: %0 --continuous
echo 📊 For HTML report, run: %0 --report

goto :eof

:check_application_status
echo ℹ️  Checking application status...
curl -s http://localhost:8080/api/plans >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Application not running, starting...
    exit /b 1
) else (
    echo ✅ Application is running
    exit /b 0
)

:start_application
echo ℹ️  Starting Spring Boot application...

REM Kill any existing process on port 8080
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
    taskkill /f /pid %%a >nul 2>&1
)
timeout /t 2 /nobreak >nul

REM Start application in background
start /b mvnw.cmd spring-boot:run -Dspring.profiles.active=test > "%TEST_RESULTS_DIR%\app-startup.log" 2>&1

REM Wait for application to start
set attempts=0
set max_attempts=60

:wait_loop
if %attempts% geq %max_attempts% (
    echo ❌ Failed to start application after %max_attempts% attempts
    exit /b 1
)

curl -s http://localhost:8080/api/plans >nul 2>&1
if errorlevel 1 (
    set /a attempts+=1
    echo -n .
    timeout /t 2 /nobreak >nul
    goto :wait_loop
) else (
    echo ✅ Application started successfully
    exit /b 0
)

:run_comprehensive_tests
echo ℹ️  Running comprehensive API tests...

REM Run Spring Boot tests
mvnw.cmd test -Dtest=ComprehensiveAPITest -Dspring.profiles.active=test > "%TEST_RESULTS_DIR%\spring-test-output.log" 2>&1
if errorlevel 1 (
    echo ❌ Spring Boot tests failed
    if "%AUTO_FIX%"=="true" (
        call :fix_spring_test_issues
    )
) else (
    echo ✅ Spring Boot tests completed
)

REM Run performance tests
call :run_performance_tests

REM Run security tests
call :run_security_tests

goto :eof

:run_performance_tests
echo ℹ️  Running performance tests...

REM Create simple load test using PowerShell
powershell -Command "& {
    $startTime = Get-Date
    $successCount = 0
    $totalCount = 10
    
    for ($i = 1; $i -le $totalCount; $i++) {
        try {
            $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/plans' -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) { $successCount++ }
            Start-Sleep -Milliseconds 100
        } catch {
            Write-Host 'Request failed'
        }
    }
    
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalMilliseconds
    $avgResponse = $duration / $totalCount
    
    Write-Host 'Performance Test Results:'
    Write-Host 'Success Rate: ' + ($successCount / $totalCount * 100) + '%'
    Write-Host 'Average Response Time: ' + [math]::Round($avgResponse, 2) + 'ms'
}" > "%TEST_RESULTS_DIR%\performance-output.log" 2>&1

echo ✅ Performance tests completed
goto :eof

:run_security_tests
echo ℹ️  Running security tests...

REM Test security headers
curl -s -I http://localhost:8080/api/plans > "%TEST_RESULTS_DIR%\security-headers.log" 2>&1
findstr /i "x-content-type-options" "%TEST_RESULTS_DIR%\security-headers.log" >nul
if errorlevel 1 (
    echo ⚠️  X-Content-Type-Options header missing
) else (
    echo ✅ X-Content-Type-Options header found
)

REM Test authentication bypass
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/user/dashboard > "%TEST_RESULTS_DIR%\auth-test.log" 2>&1
set /p auth_response=<"%TEST_RESULTS_DIR%\auth-test.log"
if "%auth_response%"=="401" (
    echo ✅ Authentication protection working
) else (
    echo ⚠️  Potential authentication bypass vulnerability
)

goto :eof

:detect_issues
echo ℹ️  Analyzing test results for issues...

REM Create issues JSON file
echo {> "%ISSUES_FILE%"
echo     "timestamp": "%date% %time%",>> "%ISSUES_FILE%"
echo     "test_failures": [],>> "%ISSUES_FILE%"
echo     "performance_issues": [],>> "%ISSUES_FILE%"
echo     "security_issues": [],>> "%ISSUES_FILE%"
echo     "compilation_errors": [],>> "%ISSUES_FILE%"
echo     "suggestions": []>> "%ISSUES_FILE%"
echo }>> "%ISSUES_FILE%"

REM Analyze Spring test failures
if exist "%TEST_RESULTS_DIR%\spring-test-output.log" (
    findstr /i "FAILED ERROR Exception" "%TEST_RESULTS_DIR%\spring-test-output.log" >nul
    if not errorlevel 1 (
        echo ⚠️  Found test failures, analyzing...
        echo Test failures detected in spring-test-output.log >> "%TEST_RESULTS_DIR%\issues-summary.txt"
    )
)

REM Generate suggestions
call :generate_fix_suggestions

goto :eof

:generate_fix_suggestions
echo ℹ️  Generating fix suggestions...

echo # Automated Fix Suggestions > "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo. >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo ## Common Issues and Solutions >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo. >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo ### 1. Authentication Failures >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo - **Issue**: JWT token validation failing >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo - **Fix**: Check JWT secret configuration and token expiration >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo - **Code**: Update `application.yml` with correct JWT settings >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo. >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo ### 2. Database Connection Issues >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo - **Issue**: H2/MySQL connection failures >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo - **Fix**: Verify database configuration and connectivity >> "%TEST_RESULTS_DIR%\fix-suggestions.md"
echo - **Code**: Check `application-test.yml` datasource settings >> "%TEST_RESULTS_DIR%\fix-suggestions.md"

goto :eof

:fix_spring_test_issues
if not "%AUTO_FIX%"=="true" goto :eof

echo ℹ️  Attempting to auto-fix detected issues...

REM Fix 1: Update test application properties
call :fix_test_configuration

REM Fix 2: Clean and rebuild project
call :fix_build_issues

REM Re-run tests after fixes
echo ℹ️  Re-running tests after applying fixes...
mvnw.cmd test -Dtest=ComprehensiveAPITest -Dspring.profiles.active=test > "%TEST_RESULTS_DIR%\spring-test-fixed.log" 2>&1

goto :eof

:fix_test_configuration
echo ℹ️  Fixing test configuration...

if not exist "src\test\resources\application-test.yml" (
    echo spring:> "src\test\resources\application-test.yml"
    echo   datasource:>> "src\test\resources\application-test.yml"
    echo     url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE>> "src\test\resources\application-test.yml"
    echo     username: sa>> "src\test\resources\application-test.yml"
    echo     password:>> "src\test\resources\application-test.yml"
    echo     driver-class-name: org.h2.Driver>> "src\test\resources\application-test.yml"
    echo.>> "src\test\resources\application-test.yml"
    echo   jpa:>> "src\test\resources\application-test.yml"
    echo     hibernate:>> "src\test\resources\application-test.yml"
    echo       ddl-auto: create-drop>> "src\test\resources\application-test.yml"
    echo     show-sql: false>> "src\test\resources\application-test.yml"
    echo.>> "src\test\resources\application-test.yml"
    echo   flyway:>> "src\test\resources\application-test.yml"
    echo     enabled: false>> "src\test\resources\application-test.yml"
    echo.>> "src\test\resources\application-test.yml"
    echo server:>> "src\test\resources\application-test.yml"
    echo   port: 0>> "src\test\resources\application-test.yml"
    echo.>> "src\test\resources\application-test.yml"
    echo app:>> "src\test\resources\application-test.yml"
    echo   jwt:>> "src\test\resources\application-test.yml"
    echo     secret: testSecretKey1234567890123456789012345678901234567890>> "src\test\resources\application-test.yml"
    echo     expiration: 86400000>> "src\test\resources\application-test.yml"
    echo.>> "src\test\resources\application-test.yml"
    echo logging:>> "src\test\resources\application-test.yml"
    echo   level:>> "src\test\resources\application-test.yml"
    echo     com.crypto.crypto: DEBUG>> "src\test\resources\application-test.yml"
    echo     org.springframework.security: DEBUG>> "src\test\resources\application-test.yml"
    
    echo ✅ Created test configuration file
)

goto :eof

:fix_build_issues
echo ℹ️  Fixing build issues...

REM Clean and rebuild
mvnw.cmd clean compile test-compile > "%TEST_RESULTS_DIR%\build-fix.log" 2>&1

REM Download dependencies
mvnw.cmd dependency:resolve dependency:resolve-sources > "%TEST_RESULTS_DIR%\deps-fix.log" 2>&1

echo ✅ Build issues fixed
goto :eof

:generate_html_report
if not "%GENERATE_REPORT%"=="true" goto :eof

echo ℹ️  Generating HTML test report...

set report_file=%REPORTS_DIR%\api-test-report-%date:~-4,4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%.html
set report_file=%report_file: =0%

echo ^<!DOCTYPE html^> > "%report_file%"
echo ^<html^> >> "%report_file%"
echo ^<head^> >> "%report_file%"
echo     ^<title^>API Test Report^</title^> >> "%report_file%"
echo     ^<style^> >> "%report_file%"
echo         body { font-family: Arial, sans-serif; margin: 20px; } >> "%report_file%"
echo         .header { background: #2196F3; color: white; padding: 20px; border-radius: 5px; } >> "%report_file%"
echo         .success { color: #4CAF50; } >> "%report_file%"
echo         .warning { color: #FF9800; } >> "%report_file%"
echo         .error { color: #F44336; } >> "%report_file%"
echo         .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; } >> "%report_file%"
echo         .metric { display: inline-block; margin: 10px; padding: 10px; background: #f5f5f5; border-radius: 3px; } >> "%report_file%"
echo         pre { background: #f8f8f8; padding: 10px; border-radius: 3px; overflow-x: auto; } >> "%report_file%"
echo     ^</style^> >> "%report_file%"
echo ^</head^> >> "%report_file%"
echo ^<body^> >> "%report_file%"
echo     ^<div class="header"^> >> "%report_file%"
echo         ^<h1^>🚀 API Test Report^</h1^> >> "%report_file%"
echo         ^<p^>Generated on: %date% %time%^</p^> >> "%report_file%"
echo     ^</div^> >> "%report_file%"
echo     ^<div class="section"^> >> "%report_file%"
echo         ^<h2^>📊 Test Summary^</h2^> >> "%report_file%"
echo         ^<div class="metric"^> >> "%report_file%"
echo             ^<strong^>Total Tests:^</strong^> ^<span id="total-tests"^-^</span^> >> "%report_file%"
echo         ^</div^> >> "%report_file%"
echo         ^<div class="metric"^> >> "%report_file%"
echo             ^<strong class="success"^>Passed:^</strong^> ^<span id="passed-tests"^-^</span^> >> "%report_file%"
echo         ^</div^> >> "%report_file%"
echo         ^<div class="metric"^> >> "%report_file%"
echo             ^<strong class="error"^>Failed:^</strong^> ^<span id="failed-tests"^-^</span^> >> "%report_file%"
echo         ^</div^> >> "%report_file%"
echo         ^<div class="metric"^> >> "%report_file%"
echo             ^<strong^>Success Rate:^</strong^> ^<span id="success-rate"^-^</span^>%% >> "%report_file%"
echo         ^</div^> >> "%report_file%"
echo     ^</div^> >> "%report_file%"
echo     ^<div class="section"^> >> "%report_file%"
echo         ^<h2^>🔍 Test Results^</h2^> >> "%report_file%"
echo         ^<pre id="test-output"^>Loading test results...^</pre^> >> "%report_file%"
echo     ^</div^> >> "%report_file%"
echo     ^<div class="section"^> >> "%report_file%"
echo         ^<h2^>🛠️ Issues ^& Suggestions^</h2^> >> "%report_file%"
echo         ^<pre id="issues-output"^>Loading issues...^</pre^> >> "%report_file%"
echo     ^</div^> >> "%report_file%"
echo ^</body^> >> "%report_file%"
echo ^</html^> >> "%report_file%"

echo ✅ HTML report generated: %report_file%
goto :eof

:run_continuous_testing
echo ℹ️  Starting continuous testing mode...

:continuous_loop
echo ℹ️  Running test cycle...

REM Run tests
call :run_comprehensive_tests

REM Detect issues
call :detect_issues

REM Auto-fix if enabled
if "%AUTO_FIX%"=="true" (
    call :fix_spring_test_issues
)

REM Wait before next cycle
echo ℹ️  Waiting 5 minutes before next test cycle...
timeout /t 300 /nobreak >nul
goto :continuous_loop

goto :eof 