@echo off
echo ========================================
echo Crypto Investment Platform - Rebuild and Test
echo ========================================
echo.

echo Cleaning and rebuilding the application...
call mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed! Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo Build successful! Starting the application...
echo.
echo The application will start on http://localhost:8080
echo.
echo Available health check endpoints:
echo - http://localhost:8080/api/test/ping
echo - http://localhost:8080/api/test/health
echo - http://localhost:8080/actuator/health
echo.
echo Press Ctrl+C to stop the application
echo.

call mvn spring-boot:run

pause 