@echo off
echo Starting Crypto Backend Server...
echo.
echo Make sure you have:
echo 1. Java 17+ installed
echo 2. MySQL/XAMPP running
echo 3. Database 'crypto_db' created
echo.
echo Starting Spring Boot application...
echo.

cd crypto
call mvnw spring-boot:run

pause 