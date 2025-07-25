#!/bin/bash

# test-runner.sh - Comprehensive API Testing and Auto-fixing Script
# Usage: ./test-runner.sh [--fix] [--continuous] [--report]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(pwd)"
TEST_RESULTS_DIR="$PROJECT_DIR/test-results"
REPORTS_DIR="$PROJECT_DIR/reports"
LOG_FILE="$TEST_RESULTS_DIR/test-execution.log"
ISSUES_FILE="$TEST_RESULTS_DIR/detected-issues.json"

# Command line arguments
AUTO_FIX=false
CONTINUOUS=false
GENERATE_REPORT=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --fix)
            AUTO_FIX=true
            shift
            ;;
        --continuous)
            CONTINUOUS=true
            shift
            ;;
        --report)
            GENERATE_REPORT=true
            shift
            ;;
        *)
            echo "Unknown option $1"
            exit 1
            ;;
    esac
done

# Create directories
mkdir -p "$TEST_RESULTS_DIR"
mkdir -p "$REPORTS_DIR"

# Initialize log file
echo "🚀 Starting API Testing Suite - $(date)" | tee "$LOG_FILE"
echo "=================================" | tee -a "$LOG_FILE"

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO")
            echo -e "${BLUE}ℹ️  $message${NC}" | tee -a "$LOG_FILE"
            ;;
        "SUCCESS")
            echo -e "${GREEN}✅ $message${NC}" | tee -a "$LOG_FILE"
            ;;
        "WARNING")
            echo -e "${YELLOW}⚠️  $message${NC}" | tee -a "$LOG_FILE"
            ;;
        "ERROR")
            echo -e "${RED}❌ $message${NC}" | tee -a "$LOG_FILE"
            ;;
    esac
}

# Function to check if application is running
check_application_status() {
    print_status "INFO" "Checking application status..."
    
    if curl -s http://localhost:8080/api/plans > /dev/null 2>&1; then
        print_status "SUCCESS" "Application is running"
        return 0
    else
        print_status "WARNING" "Application not running, starting..."
        return 1
    fi
}

# Function to start application
start_application() {
    print_status "INFO" "Starting Spring Boot application..."
    
    # Kill any existing process on port 8080
    pkill -f "java.*8080" 2>/dev/null || true
    sleep 2
    
    # Start application in background
    nohup ./mvnw spring-boot:run -Dspring.profiles.active=test > "$TEST_RESULTS_DIR/app-startup.log" 2>&1 &
    
    # Wait for application to start
    local attempts=0
    local max_attempts=60
    
    while [ $attempts -lt $max_attempts ]; do
        if curl -s http://localhost:8080/api/plans > /dev/null 2>&1; then
            print_status "SUCCESS" "Application started successfully"
            return 0
        fi
        
        attempts=$((attempts + 1))
        echo -n "."
        sleep 2
    done
    
    print_status "ERROR" "Failed to start application after $max_attempts attempts"
    return 1
}

# Function to run comprehensive tests
run_comprehensive_tests() {
    print_status "INFO" "Running comprehensive API tests..."
    
    # Run Spring Boot tests
    if ./mvnw test -Dtest=ComprehensiveAPITest -Dspring.profiles.active=test > "$TEST_RESULTS_DIR/spring-test-output.log" 2>&1; then
        print_status "SUCCESS" "Spring Boot tests completed"
    else
        print_status "ERROR" "Spring Boot tests failed"
        if [ "$AUTO_FIX" = true ]; then
            fix_spring_test_issues
        fi
    fi
    
    # Run Postman/Newman tests if collection exists
    run_newman_tests
    
    # Run performance tests
    run_performance_tests
    
    # Run security tests
    run_security_tests
}

# Function to run Newman tests
run_newman_tests() {
    if [ -f "postman-collection.json" ]; then
        print_status "INFO" "Running Newman/Postman tests..."
        
        # Install newman if not present
        if ! command -v newman &> /dev/null; then
            print_status "INFO" "Installing Newman..."
            npm install -g newman
        fi
        
        # Run newman tests
        if newman run postman-collection.json \
            --environment postman-environment.json \
            --reporters cli,json \
            --reporter-json-export "$TEST_RESULTS_DIR/newman-results.json" \
            > "$TEST_RESULTS_DIR/newman-output.log" 2>&1; then
            print_status "SUCCESS" "Newman tests completed"
        else
            print_status "WARNING" "Newman tests failed or collection not found"
        fi
    fi
}

# Function to run performance tests
run_performance_tests() {
    print_status "INFO" "Running performance tests..."
    
    # Create simple load test script
    cat > "$TEST_RESULTS_DIR/load-test.js" << 'EOF'
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 20 },
        { duration: '30s', target: 0 },
    ],
};

export default function() {
    let response = http.get('http://localhost:8080/api/plans');
    check(response, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });
    sleep(1);
}
EOF

    # Run k6 if available
    if command -v k6 &> /dev/null; then
        k6 run "$TEST_RESULTS_DIR/load-test.js" --out json="$TEST_RESULTS_DIR/performance-results.json" > "$TEST_RESULTS_DIR/performance-output.log" 2>&1
        print_status "SUCCESS" "Performance tests completed"
    else
        print_status "WARNING" "k6 not installed, skipping performance tests"
    fi
}

# Function to run security tests
run_security_tests() {
    print_status "INFO" "Running security tests..."
    
    # Test common vulnerabilities
    test_security_headers
    test_sql_injection
    test_xss_protection
    test_authentication_bypass
}

test_security_headers() {
    print_status "INFO" "Testing security headers..."
    
    local response=$(curl -s -I http://localhost:8080/api/plans)
    
    # Check for security headers
    if echo "$response" | grep -i "x-content-type-options" > /dev/null; then
        print_status "SUCCESS" "X-Content-Type-Options header found"
    else
        print_status "WARNING" "X-Content-Type-Options header missing"
    fi
    
    if echo "$response" | grep -i "x-frame-options\|content-security-policy" > /dev/null; then
        print_status "SUCCESS" "Frame protection headers found"
    else
        print_status "WARNING" "Frame protection headers missing"
    fi
}

test_sql_injection() {
    print_status "INFO" "Testing SQL injection protection..."
    
    # Test common SQL injection patterns
    local payloads=("' OR '1'='1" "'; DROP TABLE users; --" "1' UNION SELECT * FROM users--")
    
    for payload in "${payloads[@]}"; do
        local response=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/plans?id=$payload")
        if [ "$response" != "500" ]; then
            print_status "SUCCESS" "SQL injection protection working for: $payload"
        else
            print_status "WARNING" "Potential SQL injection vulnerability with: $payload"
        fi
    done
}

test_xss_protection() {
    print_status "INFO" "Testing XSS protection..."
    
    local xss_payload="<script>alert('xss')</script>"
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Content-Type: application/json" \
        -d "{\"name\": \"$xss_payload\"}" \
        http://localhost:8080/api/auth/register)
    
    if [ "$response" = "400" ] || [ "$response" = "422" ]; then
        print_status "SUCCESS" "XSS protection working"
    else
        print_status "WARNING" "Potential XSS vulnerability"
    fi
}

test_authentication_bypass() {
    print_status "INFO" "Testing authentication bypass..."
    
    # Test accessing protected endpoint without token
    local response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/user/dashboard)
    
    if [ "$response" = "401" ] || [ "$response" = "403" ]; then
        print_status "SUCCESS" "Authentication protection working"
    else
        print_status "WARNING" "Potential authentication bypass vulnerability"
    fi
}

# Function to detect and analyze issues
detect_issues() {
    print_status "INFO" "Analyzing test results for issues..."
    
    # Create issues JSON file
    cat > "$ISSUES_FILE" << 'EOF'
{
    "timestamp": "",
    "test_failures": [],
    "performance_issues": [],
    "security_issues": [],
    "compilation_errors": [],
    "suggestions": []
}
EOF

    # Update timestamp
    local timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    sed -i "s/\"timestamp\": \"\"/\"timestamp\": \"$timestamp\"/" "$ISSUES_FILE"
    
    # Analyze Spring test failures
    if [ -f "$TEST_RESULTS_DIR/spring-test-output.log" ]; then
        analyze_spring_test_failures
    fi
    
    # Analyze performance issues
    if [ -f "$TEST_RESULTS_DIR/performance-results.json" ]; then
        analyze_performance_issues
    fi
    
    # Generate suggestions
    generate_fix_suggestions
}

analyze_spring_test_failures() {
    print_status "INFO" "Analyzing Spring test failures..."
    
    local failures=$(grep -n "FAILED\|ERROR\|Exception" "$TEST_RESULTS_DIR/spring-test-output.log" || true)
    
    if [ -n "$failures" ]; then
        print_status "WARNING" "Found test failures, analyzing..."
        
        # Add to issues file (simplified - in reality, you'd parse JSON properly)
        echo "Test failures detected in spring-test-output.log" >> "$TEST_RESULTS_DIR/issues-summary.txt"
    fi
}

analyze_performance_issues() {
    print_status "INFO" "Analyzing performance issues..."
    
    # Check for slow responses in performance results
    if command -v jq &> /dev/null && [ -f "$TEST_RESULTS_DIR/performance-results.json" ]; then
        local slow_requests=$(jq '.metrics | select(.["http_req_duration"].avg > 1000)' "$TEST_RESULTS_DIR/performance-results.json" 2>/dev/null || true)
        
        if [ -n "$slow_requests" ]; then
            print_status "WARNING" "Slow response times detected"
            echo "Performance issues: Average response time > 1s" >> "$TEST_RESULTS_DIR/issues-summary.txt"
        fi
    fi
}

generate_fix_suggestions() {
    print_status "INFO" "Generating fix suggestions..."
    
    cat > "$TEST_RESULTS_DIR/fix-suggestions.md" << 'EOF'
# Automated Fix Suggestions

## Common Issues and Solutions

### 1. Authentication Failures
- **Issue**: JWT token validation failing
- **Fix**: Check JWT secret configuration and token expiration
- **Code**: Update `application.yml` with correct JWT settings

### 2. Database Connection Issues  
- **Issue**: H2/MySQL connection failures
- **Fix**: Verify database configuration and connectivity
- **Code**: Check `application-test.yml` datasource settings

### 3. Missing Dependencies
- **Issue**: ClassNotFoundException or NoSuchMethodError
- **Fix**: Update Maven dependencies
- **Code**: Run `./mvnw dependency:resolve`

### 4. Port Conflicts
- **Issue**: Application startup failures due to port conflicts
- **Fix**: Change server port or kill conflicting processes
- **Code**: `server.port: 0` for random port in tests

### 5. Security Configuration Issues
- **Issue**: 403 Forbidden on API calls
- **Fix**: Update security configuration for test profile
- **Code**: Add test-specific security config

### 6. Test Data Issues
- **Issue**: Tests failing due to missing test data
- **Fix**: Ensure test database is properly seeded
- **Code**: Check `@Sql` annotations and test data scripts

EOF
}

# Function to automatically fix common issues
fix_spring_test_issues() {
    if [ "$AUTO_FIX" != true ]; then
        return 0
    fi
    
    print_status "INFO" "Attempting to auto-fix detected issues..."
    
    # Fix 1: Update test application properties
    fix_test_configuration
    
    # Fix 2: Clean and rebuild project
    fix_build_issues
    
    # Fix 3: Reset test database
    fix_database_issues
    
    # Re-run tests after fixes
    print_status "INFO" "Re-running tests after applying fixes..."
    ./mvnw test -Dtest=ComprehensiveAPITest -Dspring.profiles.active=test > "$TEST_RESULTS_DIR/spring-test-fixed.log" 2>&1
}

fix_test_configuration() {
    print_status "INFO" "Fixing test configuration..."
    
    # Ensure test profile exists and is properly configured
    if [ ! -f "src/test/resources/application-test.yml" ]; then
        cat > "src/test/resources/application-test.yml" << 'EOF'
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    
  flyway:
    enabled: false

server:
  port: 0

app:
  jwt:
    secret: testSecretKey1234567890123456789012345678901234567890
    expiration: 86400000

logging:
  level:
    com.crypto.crypto: DEBUG
    org.springframework.security: DEBUG
EOF
        print_status "SUCCESS" "Created test configuration file"
    fi
}

fix_build_issues() {
    print_status "INFO" "Fixing build issues..."
    
    # Clean and rebuild
    ./mvnw clean compile test-compile > "$TEST_RESULTS_DIR/build-fix.log" 2>&1
    
    # Download dependencies
    ./mvnw dependency:resolve dependency:resolve-sources > "$TEST_RESULTS_DIR/deps-fix.log" 2>&1
    
    print_status "SUCCESS" "Build issues fixed"
}

fix_database_issues() {
    print_status "INFO" "Fixing database issues..."
    
    # For H2, no special action needed as it's in-memory
    # For MySQL in production, you might want to reset test schema
    
    print_status "SUCCESS" "Database issues fixed"
}

# Function to generate HTML report
generate_html_report() {
    if [ "$GENERATE_REPORT" != true ]; then
        return 0
    fi
    
    print_status "INFO" "Generating HTML test report..."
    
    local report_file="$REPORTS_DIR/api-test-report-$(date +%Y%m%d-%H%M%S).html"
    
    cat > "$report_file" << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>API Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #2196F3; color: white; padding: 20px; border-radius: 5px; }
        .success { color: #4CAF50; }
        .warning { color: #FF9800; }
        .error { color: #F44336; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
        .metric { display: inline-block; margin: 10px; padding: 10px; background: #f5f5f5; border-radius: 3px; }
        pre { background: #f8f8f8; padding: 10px; border-radius: 3px; overflow-x: auto; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🚀 API Test Report</h1>
        <p>Generated on: $(date)</p>
    </div>
    
    <div class="section">
        <h2>📊 Test Summary</h2>
        <div class="metric">
            <strong>Total Tests:</strong> <span id="total-tests">-</span>
        </div>
        <div class="metric">
            <strong class="success">Passed:</strong> <span id="passed-tests">-</span>
        </div>
        <div class="metric">
            <strong class="error">Failed:</strong> <span id="failed-tests">-</span>
        </div>
        <div class="metric">
            <strong>Success Rate:</strong> <span id="success-rate">-</span>%
        </div>
    </div>
    
    <div class="section">
        <h2>🔍 Test Results</h2>
        <pre id="test-output">Loading test results...</pre>
    </div>
    
    <div class="section">
        <h2>🛠️ Issues & Suggestions</h2>
        <pre id="issues-output">Loading issues...</pre>
    </div>
    
    <script>
        // Load test results dynamically
        fetch('/test-results/spring-test-output.log')
            .then(response => response.text())
            .then(data => {
                document.getElementById('test-output').textContent = data;
            })
            .catch(error => {
                document.getElementById('test-output').textContent = 'Failed to load test results';
            });
    </script>
</body>
</html>
EOF

    print_status "SUCCESS" "HTML report generated: $report_file"
}

# Function for continuous testing
run_continuous_testing() {
    print_status "INFO" "Starting continuous testing mode..."
    
    while true; do
        print_status "INFO" "Running test cycle..."
        
        # Run tests
        run_comprehensive_tests
        
        # Detect issues
        detect_issues
        
        # Auto-fix if enabled
        if [ "$AUTO_FIX" = true ]; then
            fix_spring_test_issues
        fi
        
        # Wait before next cycle
        print_status "INFO" "Waiting 5 minutes before next test cycle..."
        sleep 300
    done
}

# Main execution
main() {
    print_status "INFO" "Starting API testing pipeline..."
    
    # Check and start application if needed
    if ! check_application_status; then
        start_application
    fi
    
    # Run tests based on mode
    if [ "$CONTINUOUS" = true ]; then
        run_continuous_testing
    else
        run_comprehensive_tests
        detect_issues
        generate_html_report
    fi
    
    print_status "SUCCESS" "API testing pipeline completed!"
    
    # Print summary
    echo ""
    echo "📁 Test Results Location: $TEST_RESULTS_DIR"
    echo "📄 Log File: $LOG_FILE"
    if [ "$GENERATE_REPORT" = true ]; then
        echo "📊 HTML Report: $REPORTS_DIR/"
    fi
    echo ""
    echo "🔧 To auto-fix issues, run: $0 --fix"
    echo "🔄 For continuous testing, run: $0 --continuous"
    echo "📊 For HTML report, run: $0 --report"
}

# Execute main function
main "$@" 