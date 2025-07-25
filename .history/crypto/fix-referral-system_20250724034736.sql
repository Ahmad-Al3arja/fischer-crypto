-- Fix Database Schema and Admin User for Referral Code Issue
-- Run this script in phpMyAdmin

USE investment_platform;

-- Step 1: Check current admin user
SELECT id, username, display_username, phone_number, status, role FROM users WHERE phone_number = '1234567890';

-- Step 2: If username column still exists, update it to display_username
-- (Only run if the column rename didn't work previously)
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                     WHERE TABLE_NAME = 'users' 
                     AND COLUMN_NAME = 'username' 
                     AND TABLE_SCHEMA = 'investment_platform');

SET @display_column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                             WHERE TABLE_NAME = 'users' 
                             AND COLUMN_NAME = 'display_username' 
                             AND TABLE_SCHEMA = 'investment_platform');

-- If username exists but display_username doesn't, rename it
SET @sql = IF(@column_exists > 0 AND @display_column_exists = 0, 
    'ALTER TABLE users CHANGE COLUMN username display_username VARCHAR(50) NOT NULL', 
    'SELECT "Column already renamed" as status');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- If both columns exist, copy username to display_username and drop username
SET @sql2 = IF(@column_exists > 0 AND @display_column_exists > 0, 
    'UPDATE users SET display_username = username WHERE display_username IS NULL OR display_username = ""', 
    'SELECT "No update needed" as status');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Drop username column if it still exists
SET @sql3 = IF(@column_exists > 0 AND @display_column_exists > 0, 
    'ALTER TABLE users DROP COLUMN username', 
    'SELECT "Column already dropped" as status');
PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- Step 3: Ensure admin user has correct display_username
UPDATE users SET display_username = 'admin' WHERE phone_number = '1234567890';

-- Step 4: Create unique index on display_username if it doesn't exist
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
                    WHERE TABLE_NAME = 'users' 
                    AND INDEX_NAME = 'idx_users_display_username' 
                    AND TABLE_SCHEMA = 'investment_platform');

SET @sql4 = IF(@index_exists = 0, 
    'CREATE UNIQUE INDEX idx_users_display_username ON users(display_username)', 
    'SELECT "Index already exists" as status');
PREPARE stmt4 FROM @sql4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

-- Step 5: Verify admin user setup
SELECT 
    id, 
    display_username, 
    phone_number, 
    status, 
    role,
    'Admin user ready for referrals' as message
FROM users 
WHERE phone_number = '1234567890';

-- Step 6: Show all users for verification
SELECT 
    id, 
    display_username, 
    phone_number, 
    status, 
    role
FROM users 
ORDER BY id; 