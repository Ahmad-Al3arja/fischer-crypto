@echo off
REM Production Deployment Script for Investment Platform (Windows)
REM This script sets up the environment variables and deploys the application

echo 🚀 Starting Production Deployment...

REM Check if required environment variables are set
if "%DB_PASSWORD%"=="" (
    echo ❌ ERROR: DB_PASSWORD environment variable is not set!
    echo Please set it with: set DB_PASSWORD=your_secure_password
    exit /b 1
)

if "%JWT_SECRET%"=="" (
    echo ❌ ERROR: JWT_SECRET environment variable is not set!
    echo Please set it with: set JWT_SECRET=your-256-bit-secret-key-here
    exit /b 1
)

REM Set default values for optional environment variables
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=3306
if "%DB_NAME%"=="" set DB_NAME=investment_platform
if "%DB_USERNAME%"=="" set DB_USERNAME=root
if "%SERVER_PORT%"=="" set SERVER_PORT=8080
if "%PLATFORM_BASE_URL%"=="" set PLATFORM_BASE_URL=https://yourapp.com
if "%PLATFORM_USDT_WALLET%"=="" set PLATFORM_USDT_WALLET=TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE
if "%MAINTENANCE_MODE%"=="" set MAINTENANCE_MODE=false

echo ✅ Environment variables configured:
echo    DB_HOST: %DB_HOST%
echo    DB_PORT: %DB_PORT%
echo    DB_NAME: %DB_NAME%
echo    DB_USERNAME: %DB_USERNAME%
echo    SERVER_PORT: %SERVER_PORT%
echo    PLATFORM_BASE_URL: %PLATFORM_BASE_URL%
echo    JWT_SECRET: [HIDDEN]
echo    DB_PASSWORD: [HIDDEN]

REM Create logs directory if it doesn't exist
if not exist "logs" mkdir logs

REM Build the application
echo 🔨 Building application...
call mvnw.cmd clean package -DskipTests -Pprod

if %ERRORLEVEL% neq 0 (
    echo ❌ Build failed!
    exit /b 1
)

echo ✅ Build completed successfully!

REM Run database migrations
echo 🗄️ Running database migrations...
java -jar target\crypto-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.flyway.migrate

if %ERRORLEVEL% neq 0 (
    echo ❌ Database migration failed!
    exit /b 1
)

echo ✅ Database migrations completed!

REM Start the application
echo 🚀 Starting application in production mode...
java -jar target\crypto-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

echo ✅ Application started successfully!
echo 🌐 Application is running on port %SERVER_PORT%
echo 📊 Health check: http://localhost:%SERVER_PORT%/actuator/health 