# Investment Platform - Postman Collection Guide

## Overview

This guide covers the comprehensive Postman collection for testing the Investment Platform API. The collection includes tests for all major functionality including authentication, user management, transactions, admin features, and security scenarios.

## Collection Structure

### 📁 Main Modules

1. **🚀 Setup & Health Checks**
   - Health check endpoint
   - Test data initialization
   - Plan setup
   - Random data generation

2. **🔐 Authentication Module**
   - Admin login
   - User registration (valid/invalid cases)
   - User login/logout
   - Password management
   - Profile management

3. **📊 Plans Module**
   - Public plan access
   - Plan details retrieval
   - Error handling for non-existent plans

4. **👤 User Module**
   - Dashboard functionality
   - Profile management
   - Balance tracking
   - Referral system
   - Daily counter operations

5. **💰 Transaction Module**
   - Deposit creation and validation
   - Withdrawal processing
   - Wallet management
   - Promo code handling
   - Transaction history

6. **🔧 Admin Module**
   - User management
   - Deposit/Withdrawal approval
   - Plan management
   - Settings configuration
   - System statistics

7. **🌐 Platform Info Module**
   - Platform information retrieval
   - System status

8. **🔒 Security & Authorization Tests**
   - Authorization validation
   - JWT token handling
   - Role-based access control

9. **❌ Error Handling Tests**
   - Malformed requests
   - Validation errors
   - Edge cases

10. **🎯 Edge Cases & Boundary Tests**
    - Large numbers
    - Negative values
    - Special characters
    - SQL injection attempts
    - XSS attempts

## Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `base_url` | Base URL for the API server | `http://localhost:8080` |
| `admin_token` | JWT token for admin user | Auto-populated |
| `user_token` | JWT token for test user 1 | Auto-populated |
| `user2_token` | JWT token for test user 2 | Auto-populated |
| `admin_user_id` | Admin user ID | Auto-populated |
| `test_user_id` | Test user 1 ID | Auto-populated |
| `test_user2_id` | Test user 2 ID | Auto-populated |
| `test_plan_id` | Test plan ID for transactions | Auto-populated |
| `test_deposit_id` | Test deposit ID | Auto-populated |
| `test_withdrawal_id` | Test withdrawal ID | Auto-populated |
| `test_promo_code_id` | Test promo code ID | Auto-populated |
| `test_wallet_change_id` | Test wallet change request ID | Auto-populated |
| `random_phone` | Random phone number for testing | Auto-generated |
| `random_username` | Random username for testing | Auto-generated |

### Enhanced Variables (v2.0)

| Variable | Description | Purpose |
|----------|-------------|---------|
| `test_start_time` | Test suite start time | Performance tracking |
| `test_results` | Test results tracking | Analytics |

## Setup Instructions

### 1. Import Collection

1. Open Postman
2. Click "Import" button
3. Select the `crypto.json` file
4. The collection will be imported with all tests

### 2. Create Environment

1. Click "Environments" in the sidebar
2. Click "Create Environment"
3. Name it "Investment Platform - Local"
4. Add the required variables (see above)
5. Set `base_url` to your server URL

### 3. Run Setup Tests

1. Select the "🚀 Setup & Health Checks" folder
2. Click "Run" to execute setup tests
3. Verify all tests pass before proceeding

## Running Tests

### Individual Tests

1. Select any test in the collection
2. Click "Send" to run the test
3. View results in the response tab

### Folder Tests

1. Select a folder (e.g., "🔐 Authentication Module")
2. Click "Run" to execute all tests in the folder
3. View summary of results

### Complete Collection

1. Select the root collection
2. Click "Run" to execute all tests
3. This will run tests in the correct order

## Test Execution Order

The collection is designed to run tests in a specific order:

1. **Setup & Health Checks** - Initialize test environment
2. **Authentication Module** - Login and create test users
3. **Plans Module** - Verify plan functionality
4. **User Module** - Test user features
5. **Transaction Module** - Test financial operations
6. **Admin Module** - Test administrative functions
7. **Security Tests** - Verify security measures
8. **Error Handling** - Test error scenarios
9. **Edge Cases** - Test boundary conditions

## Enhanced Features (v2.0)

### Performance Monitoring

- Response time tracking
- Slow response warnings (>5 seconds)
- Performance metrics logging

### Test Results Tracking

- Automatic test result counting
- Pass/fail statistics
- Test execution timing

### Better Error Handling

- Detailed error logging
- Graceful failure handling
- Enhanced error messages

### Improved Test Scripts

- More comprehensive assertions
- Better data validation
- Enhanced debugging information

## Customization

### Adding New Tests

1. Create a new request in the appropriate folder
2. Add test scripts using the existing patterns
3. Include proper assertions and error handling
4. Update the test results tracking if needed

### Modifying Existing Tests

1. Locate the test in the collection
2. Modify the request or test script as needed
3. Ensure the test follows the established patterns
4. Test the modification thoroughly

### Environment-Specific Configurations

Create different environments for:
- Local development
- Staging environment
- Production testing

## Best Practices

### Test Design

1. **Descriptive Names** - Use clear, descriptive test names
2. **Proper Assertions** - Include multiple assertions per test
3. **Error Handling** - Test both success and failure scenarios
4. **Data Validation** - Verify response data structure and content
5. **Performance Checks** - Include response time assertions

### Test Scripts

```javascript
// Example test script pattern
pm.test('Test name', function () {
    pm.response.to.have.status(200);
});

pm.test('Response structure validation', function () {
    const response = pm.response.json();
    pm.expect(response).to.have.property('data');
    pm.expect(response.data).to.be.an('array');
});

pm.test('Performance check', function () {
    pm.expect(pm.response.responseTime).to.be.below(3000);
});
```

### Variable Management

1. **Auto-population** - Let tests populate variables automatically
2. **Validation** - Always validate variable values before use
3. **Cleanup** - Clear sensitive variables after tests
4. **Documentation** - Document all variables and their purposes

## Troubleshooting

### Common Issues

1. **Authentication Failures**
   - Verify admin credentials
   - Check JWT token expiration
   - Ensure proper token format

2. **Test Data Issues**
   - Run setup tests first
   - Verify test data initialization
   - Check environment variables

3. **Performance Issues**
   - Monitor response times
   - Check server resources
   - Verify network connectivity

4. **Environment Issues**
   - Verify environment selection
   - Check variable values
   - Ensure proper base URL

### Debugging Tips

1. **Console Logging** - Use `console.log()` for debugging
2. **Response Inspection** - Check response headers and body
3. **Variable Inspection** - Verify environment variables
4. **Test Isolation** - Run tests individually to isolate issues

## Security Considerations

### Test Data

1. **Sensitive Information** - Never commit real credentials
2. **Test Isolation** - Use separate test data
3. **Cleanup** - Clean up test data after tests
4. **Environment Separation** - Use different environments for different stages

### API Security

1. **Authentication** - Test all authentication scenarios
2. **Authorization** - Verify role-based access
3. **Input Validation** - Test malicious inputs
4. **Rate Limiting** - Test rate limiting behavior

## Continuous Integration

### Newman Integration

```bash
# Install Newman
npm install -g newman

# Run collection
newman run crypto.json -e environment.json

# Run with reporting
newman run crypto.json -e environment.json --reporters cli,json --reporter-json-export results.json
```

### CI/CD Pipeline

```yaml
# Example GitHub Actions workflow
- name: Run API Tests
  run: |
    newman run crypto.json -e environment.json --reporters cli,json
    newman run crypto.json -e environment.json --reporters junit --reporter-junit-export test-results.xml
```

## Reporting

### Test Results

The collection includes built-in reporting features:
- Test execution summary
- Performance metrics
- Error tracking
- Success/failure statistics

### Custom Reports

Create custom reports using:
- Newman reporters
- Postman monitors
- Custom scripts
- Third-party tools

## Maintenance

### Regular Updates

1. **API Changes** - Update tests when API changes
2. **New Features** - Add tests for new functionality
3. **Bug Fixes** - Update tests when bugs are fixed
4. **Performance** - Monitor and optimize test performance

### Version Control

1. **Backup** - Regularly backup the collection
2. **Versioning** - Use version control for changes
3. **Documentation** - Keep documentation updated
4. **Review** - Regularly review and improve tests

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review test logs and console output
3. Verify environment configuration
4. Test individual requests
5. Check API documentation

## Conclusion

This Postman collection provides comprehensive testing for the Investment Platform API. Regular maintenance and updates ensure reliable testing and early detection of issues. Follow the best practices outlined in this guide to maintain high-quality tests and efficient testing processes. 