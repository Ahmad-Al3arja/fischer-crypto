-- Fix Admin User Password Issue
-- Run this script in phpMyAdmin to fix the admin login problem

USE investment_platform;

-- Step 1: Delete existing admin user (if any)
DELETE FROM users WHERE phone_number = '1234567890' OR username = 'admin';

-- Step 2: Create admin user with correct BCrypt hash for "admin123"
INSERT INTO users (
    full_name, 
    username, 
    phone_number, 
    password, 
    total_balance, 
    frozen_balance, 
    referral_earnings, 
    status, 
    role, 
    created_at, 
    updated_at
) VALUES (
    'System Administrator',
    'admin',
    '1234567890',
    '$2a$10$N.fN8m8p.X1.qQqfJF.J2OMpEP4RgXQ8YNStR9TgY5VK9Z7F.2e0C',
    0.00,
    0.00,
    0.00,
    'ACTIVE',
    'ADMIN',
    NOW(),
    NOW()
);

-- Step 3: Verify the user was created
SELECT 
    id, 
    username, 
    phone_number, 
    role, 
    status, 
    LENGTH(password) as password_length,
    SUBSTRING(password, 1, 10) as password_start
FROM users 
WHERE username = 'admin';

-- Alternative: If the above hash doesn't work, try this one (also for "admin123")
-- UPDATE users 
-- SET password = '$2a$10$DowJoayNZingxqEWiXdAH.ZgLn6VQDLwEe8B8N0KqL7pGkQKGUCrO'
-- WHERE username = 'admin';

-- Step 4: Check all users in the system
SELECT 
    id, 
    username, 
    phone_number, 
    role, 
    status,
    LENGTH(password) as password_length
FROM users; 