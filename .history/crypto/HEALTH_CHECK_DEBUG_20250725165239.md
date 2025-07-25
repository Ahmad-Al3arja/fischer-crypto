# Health Check Endpoint Debug Guide

## Problem
You're getting an "Internal server error" with "An unexpected error occurred" when testing health check endpoints.

## Root Cause
The issue is likely caused by:
1. Missing Spring Boot Actuator dependency (now added)
2. Database connection issues
3. Unhandled exceptions in the application startup

## Solutions Applied

### 1. Added Spring Boot Actuator Dependency
- Added `spring-boot-starter-actuator` to `pom.xml`
- This provides built-in health check endpoints at `/actuator/health`

### 2. Created Custom Health Check Endpoints
- Added `/api/test/ping` - Simple connectivity test
- Added `/api/test/health` - Comprehensive health check with database test

### 3. Updated Security Configuration
- Added `/actuator/**` to permitted URLs in `SecurityConfig.java`
- This allows access to Spring Boot Actuator endpoints without authentication

## Available Health Check Endpoints

### Custom Endpoints (No Authentication Required)
- `GET /api/test/ping` - Simple ping/pong response
- `GET /api/test/health` - Health check with database connectivity test

### Spring Boot Actuator Endpoints (No Authentication Required)
- `GET /actuator/health` - Spring Boot's built-in health check
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/env` - Environment variables

## Testing Steps

### 1. Rebuild and Restart the Application
```bash
# Navigate to the crypto directory
cd crypto

# Clean and rebuild
mvn clean install

# Start the application
mvn spring-boot:run
```

### 2. Test the Endpoints
Run the health check test script:
```powershell
.\health-check-test.ps1
```

Or test manually:
```bash
# Test ping endpoint
curl http://localhost:8080/api/test/ping

# Test custom health check
curl http://localhost:8080/api/test/health

# Test Spring Boot actuator health
curl http://localhost:8080/actuator/health
```

### 3. Check Application Logs
Look for any error messages in the console output when starting the application.

## Common Issues and Solutions

### Database Connection Issues
If you see database-related errors:
1. Make sure MySQL is running on localhost:3306
2. Check if the database `investment_platform` exists
3. Verify database credentials in `application.yml`

### Port Already in Use
If port 8080 is already in use:
1. Change the port in `application.yml`:
   ```yaml
   server:
     port: 8081  # or any available port
   ```

### Missing Dependencies
If you see class not found errors:
1. Run `mvn clean install` to rebuild
2. Check that all dependencies are properly resolved

## Expected Responses

### Successful Health Check Response
```json
{
  "status": "UP",
  "message": "Application is healthy",
  "timestamp": 1234567890123,
  "details": {
    "database": "UP",
    "plans_count": 9,
    "application": "UP",
    "version": "1.0.0"
  }
}
```

### Successful Ping Response
```json
{
  "message": "pong",
  "timestamp": 1234567890123
}
```

### Spring Boot Actuator Health Response
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 419430400000,
        "threshold": 10485760
      }
    }
  }
}
```

## Debugging Tips

1. **Check Application Startup**: Look for any error messages during application startup
2. **Database Connectivity**: Ensure MySQL is running and accessible
3. **Port Availability**: Make sure port 8080 is not used by another application
4. **Dependencies**: Ensure all Maven dependencies are properly resolved
5. **Logs**: Check the application logs for detailed error information

## Next Steps

If you're still experiencing issues:
1. Run the health check test script and share the output
2. Check the application startup logs for any error messages
3. Verify that MySQL is running and accessible
4. Try accessing the endpoints with a tool like Postman or curl 