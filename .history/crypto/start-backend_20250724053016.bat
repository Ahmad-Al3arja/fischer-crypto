@echo off
echo Starting Crypto Investment Platform Backend...
echo.

echo Please make sure:
echo 1. XAMPP/MySQL is running on port 3306
echo 2. Database 'investment_platform' exists
echo 3. MySQL root password is set (or leave empty if no password)
echo.

set /p DB_PASSWORD="Enter MySQL root password (or press Enter if no password): "

if "%DB_PASSWORD%"=="" (
    set DB_PASSWORD=
) else (
    set DB_PASSWORD=%DB_PASSWORD%
)

echo.
echo Starting application with database password: %DB_PASSWORD%
echo.

set DB_USERNAME=root
set DB_PASSWORD=%DB_PASSWORD%
set JWT_SECRET=mySecretKey1234567890123456789012345678901234567890

mvn spring-boot:run

pause 