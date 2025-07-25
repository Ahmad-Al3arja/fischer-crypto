-- V6__Fix_referral_system.sql
-- Fix referral system and ensure admin user exists

-- Step 1: Ensure users table has the correct structure
-- Check if username column still exists and rename it if needed
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                     WHERE TABLE_NAME = 'users' 
                     AND COLUMN_NAME = 'username' 
                     AND TABLE_SCHEMA = DATABASE());

SET @display_column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                             WHERE TABLE_NAME = 'users' 
                             AND COLUMN_NAME = 'display_username' 
                             AND TABLE_SCHEMA = DATABASE());

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

-- Step 2: Create unique index on display_username if it doesn't exist
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
                    WHERE TABLE_NAME = 'users' 
                    AND INDEX_NAME = 'idx_users_display_username' 
                    AND TABLE_SCHEMA = DATABASE());

SET @sql4 = IF(@index_exists = 0, 
    'CREATE UNIQUE INDEX idx_users_display_username ON users(display_username)', 
    'SELECT "Index already exists" as status');
PREPARE stmt4 FROM @sql4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

-- Step 3: Ensure admin user exists with correct display_username
-- First, check if admin user exists
SET @admin_exists = (SELECT COUNT(*) FROM users WHERE phone_number = '1234567890');

-- If admin doesn't exist, create it
SET @sql5 = IF(@admin_exists = 0, 
    'INSERT INTO users (full_name, display_username, phone_number, password, total_balance, frozen_balance, referral_earnings, status, role, created_at, updated_at) VALUES ("Admin User", "admin", "1234567890", "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi", 0.00, 0.00, 0.00, "ACTIVE", "ADMIN", NOW(), NOW())', 
    'SELECT "Admin already exists" as status');
PREPARE stmt5 FROM @sql5;
EXECUTE stmt5;
DEALLOCATE PREPARE stmt5;

-- If admin exists, ensure display_username is correct
UPDATE users SET display_username = 'admin' WHERE phone_number = '1234567890';

-- Step 4: Verify admin user setup
SELECT 
    id, 
    display_username, 
    phone_number, 
    status, 
    role,
    'Admin user ready for referrals' as message
FROM users 
WHERE phone_number = '1234567890'; 