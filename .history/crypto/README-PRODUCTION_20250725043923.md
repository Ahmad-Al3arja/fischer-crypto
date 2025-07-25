# Production Deployment Guide

## 🔒 Security Requirements

### Required Environment Variables

**CRITICAL: These must be set before deployment:**

```bash
# Database Configuration
export DB_PASSWORD=your_secure_password_here
export DB_USERNAME=your_db_username

# JWT Security (256-bit minimum)
export JWT_SECRET=your-256-bit-secret-key-here-make-it-very-long-and-random

# Optional Configuration
export DB_HOST=your_db_host
export DB_PORT=3306
export DB_NAME=investment_platform
export SERVER_PORT=8080
export PLATFORM_BASE_URL=https://yourdomain.com
export PLATFORM_USDT_WALLET=your_usdt_wallet_address
export MAINTENANCE_MODE=false
```

### Windows Environment Variables

```cmd
set DB_PASSWORD=your_secure_password_here
set JWT_SECRET=your-256-bit-secret-key-here-make-it-very-long-and-random
set DB_USERNAME=your_db_username
```

## 🚀 Deployment Steps

### 1. Set Environment Variables

**Linux/Mac:**
```bash
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your-256-bit-secret-key-here
```

**Windows:**
```cmd
set DB_PASSWORD=your_secure_password
set JWT_SECRET=your-256-bit-secret-key-here
```

### 2. Run Deployment Script

**Linux/Mac:**
```bash
chmod +x deploy-production.sh
./deploy-production.sh
```

**Windows:**
```cmd
deploy-production.bat
```

### 3. Manual Deployment (Alternative)

```bash
# Build with production profile
./mvnw clean package -DskipTests -Pprod

# Run with production profile
java -jar target/crypto-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 🔧 Production Configuration

The application uses `application-prod.yml` for production settings:

- **Database**: SSL enabled, validation mode
- **JPA**: No auto-table creation
- **Flyway**: Clean disabled, validation enabled
- **Logging**: File-based logging to `logs/application.log`
- **Security**: Reduced debug logging
- **Management**: Limited endpoints (health, info only)

## 🛡️ Security Checklist

- [ ] Strong database password set
- [ ] 256-bit JWT secret configured
- [ ] SSL enabled for database connection
- [ ] Production profile active
- [ ] Debug logging disabled
- [ ] Management endpoints limited
- [ ] Database migrations validated
- [ ] No auto-table creation enabled

## 📊 Health Monitoring

After deployment, check application health:

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

## 🔍 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify DB_PASSWORD is set correctly
   - Check database server is running
   - Ensure database exists

2. **JWT Secret Not Set**
   - Set JWT_SECRET environment variable
   - Use at least 256-bit key

3. **Migration Failed**
   - Check database permissions
   - Verify Flyway schema history table

4. **Port Already in Use**
   - Change SERVER_PORT environment variable
   - Kill existing process on port

### Logs

Check application logs:
```bash
tail -f logs/application.log
```

## 🔄 Database Migrations

The application automatically runs migrations on startup:

- V1: Complete schema setup
- V2: Add current_day column to daily_counters

To manually run migrations:
```bash
java -jar target/crypto-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.flyway.migrate
```

## 📝 Environment Variables Reference

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| DB_PASSWORD | ✅ | - | Database password |
| JWT_SECRET | ✅ | - | JWT signing secret (256-bit) |
| DB_USERNAME | ❌ | root | Database username |
| DB_HOST | ❌ | localhost | Database host |
| DB_PORT | ❌ | 3306 | Database port |
| DB_NAME | ❌ | investment_platform | Database name |
| SERVER_PORT | ❌ | 8080 | Application port |
| PLATFORM_BASE_URL | ❌ | https://yourapp.com | Platform base URL |
| PLATFORM_USDT_WALLET | ❌ | TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE | USDT wallet address |
| MAINTENANCE_MODE | ❌ | false | Maintenance mode flag | 