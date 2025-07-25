-- Fix Admin User Script
-- Run this in phpMyAdmin to fix the admin user

USE investment_platform;

-- Check current admin user
SELECT id, username, phone_number, password, status, role, created_at FROM users WHERE username = 'admin';

-- Update admin user with correct password hash and status
UPDATE users 
SET 
    password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    status = 'ACTIVE',
    role = 'ADMIN',
    created_at = NOW(),
    updated_at = NOW()
WHERE username = 'admin';

-- If admin user doesn't exist, create it
INSERT INTO users (full_name, username, phone_number, password, total_balance, frozen_balance, referral_earnings, status, role, created_at, updated_at)
SELECT 
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
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Verify the admin user
SELECT id, username, phone_number, password, status, role, created_at FROM users WHERE username = 'admin'; 