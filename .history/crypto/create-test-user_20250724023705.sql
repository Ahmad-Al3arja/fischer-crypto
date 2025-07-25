-- Create Test User for API Testing
-- Run this in phpMyAdmin after the main database setup

USE investment_platform;

-- First, let's check if admin user exists and is active
SELECT id, username, phone_number, status, role FROM users WHERE username = 'admin';

-- If admin user doesn't exist or is not active, create/update it
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
    'Admin User', 
    'admin', 
    '1234567890', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
    0.00, 
    0.00, 
    0.00, 
    'ACTIVE', 
    'ADMIN', 
    NOW(), 
    NOW()
) ON DUPLICATE KEY UPDATE 
    status = 'ACTIVE',
    updated_at = NOW();

-- Create a regular test user
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
    'Test User', 
    'testuser', 
    '9876543210', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
    1000.00, 
    0.00, 
    50.00, 
    'ACTIVE', 
    'USER', 
    NOW(), 
    NOW()
) ON DUPLICATE KEY UPDATE 
    status = 'ACTIVE',
    updated_at = NOW();

-- Show all users
SELECT id, username, phone_number, status, role, created_at FROM users; 