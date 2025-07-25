# Deposit Approval - Referral Earnings Debug Guide

## Problem
When trying to approve a deposit as an admin, you get this error:
```
"Column 'referrer_id' cannot be null" 
[insert into referral_earnings (amount,commission_type,created_at,deposit_id,referred_user_id,referrer_id,user_id)]
```

## Root Cause
The issue was in the `ReferralService.addReferralEarning()` method where the `referrer` field was not being set when creating a `ReferralEarning` record. The database schema requires `referrer_id` to be NOT NULL, but the code was trying to insert a record with a null referrer.

## Solutions Applied

### 1. Fixed Missing `setReferrer()` Call
In `ReferralService.addReferralEarning()` method:
```java
// BEFORE (causing the error):
earning.setUser(referrer);
// earning.setReferrer(referrer); // This line was missing!
earning.setReferredUser(referredUser);

// AFTER (fixed):
earning.setUser(referrer);
earning.setReferrer(referrer);  // Added this line
earning.setReferredUser(referredUser);
```

### 2. Fixed Inconsistent Field Mapping
In `ReferralService.processCommission()` method:
```java
// BEFORE (inconsistent):
earning.setUser(referredUser); // Wrong - should be the referrer

// AFTER (consistent):
earning.setUser(referrer); // Correct - the person who gets the commission
```

## Database Schema Understanding

The `referral_earnings` table has these fields:
- `user_id` - The user who gets the commission (the referrer)
- `referrer_id` - The user who gets the commission (same as user_id)
- `referred_user_id` - The user who made the deposit
- `deposit_id` - The deposit that triggered the commission

## Testing the Fix

### Option 1: Use the Test Script
```powershell
.\test-deposit-approval.ps1
```

### Option 2: Manual Testing
1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Create a test user with a referrer:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "fullName": "Test User",
       "username": "testuser",
       "phoneNumber": "+1234567891",
       "password": "testpass123",
       "confirmPassword": "testpass123",
       "referralCode": "admin"
     }'
   ```

3. **Login as the user:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "phoneNumber": "+1234567891",
       "password": "testpass123"
     }'
   ```

4. **Create a deposit:**
   ```bash
   curl -X POST http://localhost:8080/api/deposits \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_USER_TOKEN" \
     -d '{
       "planId": 1,
       "amount": 100.00,
       "promoCode": ""
     }'
   ```

5. **Login as admin:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "phoneNumber": "1234567890",
       "password": "admin123"
     }'
   ```

6. **Get all deposits:**
   ```bash
   curl -X GET http://localhost:8080/api/admin/deposits \
     -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
   ```

7. **Approve the deposit:**
   ```bash
   curl -X POST http://localhost:8080/api/admin/deposits/DEPOSIT_ID/approve \
     -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
   ```

## Expected Behavior After Fix

### Successful Deposit Approval
- The deposit status should change to "APPROVED"
- The user's balance should increase by the deposit amount
- The user's plan should be updated
- Referral earnings should be created for the referrer (if any)
- No database errors should occur

### Referral Earnings Creation
When a user with a referrer makes a deposit:
- **Direct referrer** gets 12% commission
- **Grand referrer** gets 6% commission (if exists)
- Referral earnings records are created in the database
- Referrer's balance and referral earnings are updated

## Debugging Tips

1. **Check Application Logs**: Look for detailed error messages in the console
2. **Verify User Referrer**: Make sure the user has a valid referrer before testing
3. **Database State**: Check if the referral_earnings table exists and has the correct schema
4. **Transaction Rollback**: If the error occurs, the entire transaction will be rolled back

## Common Issues

### User Has No Referrer
If a user doesn't have a referrer, no referral earnings should be created. The system should handle this gracefully.

### Database Connection Issues
Make sure MySQL is running and accessible on localhost:3306.

### Missing Dependencies
Run `mvn clean install` to ensure all dependencies are properly resolved.

## Next Steps

If you're still experiencing issues:
1. Run the test script and share the output
2. Check the application startup logs for any error messages
3. Verify that the database schema is correct
4. Test with a user that has a valid referrer 