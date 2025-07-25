#!/bin/bash

# Production Deployment Script for Investment Platform
# This script sets up the environment variables and deploys the application

echo "🚀 Starting Production Deployment..."

# Check if required environment variables are set
if [ -z "$DB_PASSWORD" ]; then
    echo "❌ ERROR: DB_PASSWORD environment variable is not set!"
    echo "Please set it with: export DB_PASSWORD=your_secure_password"
    exit 1
fi

if [ -z "$JWT_SECRET" ]; then
    echo "❌ ERROR: JWT_SECRET environment variable is not set!"
    echo "Please set it with: export JWT_SECRET=your-256-bit-secret-key-here"
    exit 1
fi

# Set default values for optional environment variables
export DB_HOST=${DB_HOST:-localhost}
export DB_PORT=${DB_PORT:-3306}
export DB_NAME=${DB_NAME:-investment_platform}
export DB_USERNAME=${DB_USERNAME:-root}
export SERVER_PORT=${SERVER_PORT:-8080}
export PLATFORM_BASE_URL=${PLATFORM_BASE_URL:-https://yourapp.com}
export PLATFORM_USDT_WALLET=${PLATFORM_USDT_WALLET:-TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE}
export MAINTENANCE_MODE=${MAINTENANCE_MODE:-false}

echo "✅ Environment variables configured:"
echo "   DB_HOST: $DB_HOST"
echo "   DB_PORT: $DB_PORT"
echo "   DB_NAME: $DB_NAME"
echo "   DB_USERNAME: $DB_USERNAME"
echo "   SERVER_PORT: $SERVER_PORT"
echo "   PLATFORM_BASE_URL: $PLATFORM_BASE_URL"
echo "   JWT_SECRET: [HIDDEN]"
echo "   DB_PASSWORD: [HIDDEN]"

# Create logs directory if it doesn't exist
mkdir -p logs

# Build the application
echo "🔨 Building application..."
./mvnw clean package -DskipTests -Pprod

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build completed successfully!"

# Run database migrations
echo "🗄️ Running database migrations..."
java -jar target/crypto-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.flyway.migrate

if [ $? -ne 0 ]; then
    echo "❌ Database migration failed!"
    exit 1
fi

echo "✅ Database migrations completed!"

# Start the application
echo "🚀 Starting application in production mode..."
java -jar target/crypto-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

echo "✅ Application started successfully!"
echo "🌐 Application is running on port $SERVER_PORT"
echo "📊 Health check: http://localhost:$SERVER_PORT/actuator/health" 