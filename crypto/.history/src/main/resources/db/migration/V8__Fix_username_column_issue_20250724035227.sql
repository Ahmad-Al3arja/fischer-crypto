-- V8__Fix_username_column_issue.sql
-- Fix the username/display_username column issue

-- First, let's check what columns exist and handle accordingly
SET @username_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                       WHERE TABLE_NAME = 'users' 
                       AND COLUMN_NAME = 'username' 
                       AND TABLE_SCHEMA = DATABASE());

SET @display_username_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                               WHERE TABLE_NAME = 'users' 
                               AND COLUMN_NAME = 'display_username' 
                               AND TABLE_SCHEMA = DATABASE());

-- Case 1: Both columns exist - copy data and drop username
SET @sql1 = IF(@username_exists > 0 AND @display_username_exists > 0, 
    'UPDATE users SET display_username = COALESCE(display_username, username) WHERE display_username IS NULL OR display_username = ""', 
    'SELECT "No update needed - case 1" as status');
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- Drop username column if both exist
SET @sql2 = IF(@username_exists > 0 AND @display_username_exists > 0, 
    'ALTER TABLE users DROP COLUMN username', 
    'SELECT "No drop needed - case 1" as status');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Case 2: Only username exists - rename it to display_username
SET @sql3 = IF(@username_exists > 0 AND @display_username_exists = 0, 
    'ALTER TABLE users CHANGE COLUMN username display_username VARCHAR(50) NOT NULL', 
    'SELECT "No rename needed - case 2" as status');
PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- Case 3: Only display_username exists - do nothing
-- This is the desired state

-- Ensure display_username has unique constraint
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
                    WHERE TABLE_NAME = 'users' 
                    AND INDEX_NAME = 'display_username' 
                    AND TABLE_SCHEMA = DATABASE());

SET @sql4 = IF(@index_exists = 0, 
    'ALTER TABLE users ADD UNIQUE KEY display_username (display_username)', 
    'SELECT "Unique constraint already exists" as status');
PREPARE stmt4 FROM @sql4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

-- Ensure admin user has proper display_username
UPDATE users 
SET display_username = 'admin' 
WHERE phone_number = '1234567890' 
AND (display_username IS NULL OR display_username = '');

-- Verify the final state
SELECT 
    'Final verification' as step,
    COUNT(*) as total_users,
    COUNT(CASE WHEN display_username = 'admin' THEN 1 END) as admin_users
FROM users;