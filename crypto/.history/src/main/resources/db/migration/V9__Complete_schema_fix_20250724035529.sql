-- V9__Complete_schema_fix.sql
-- Complete fix for database schema issues

-- Step 1: Check and fix the users table structure
-- Drop any problematic constraints first
SET foreign_key_checks = 0;

-- Check if username column exists and handle it
SET @username_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                       WHERE TABLE_NAME = 'users' 
                       AND COLUMN_NAME = 'username' 
                       AND TABLE_SCHEMA = DATABASE());

SET @display_username_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                               WHERE TABLE_NAME = 'users' 
                               AND COLUMN_NAME = 'display_username' 
                               AND TABLE_SCHEMA = DATABASE());

-- If only username exists, rename it
SET @sql1 = IF(@username_exists > 0 AND @display_username_exists = 0, 
    'ALTER TABLE users CHANGE COLUMN username display_username VARCHAR(50) NOT NULL', 
    'SELECT "No rename needed - username to display_username" as status');
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- If both exist, copy data and drop username
SET @sql2 = IF(@username_exists > 0 AND @display_username_exists > 0, 
    'UPDATE users SET display_username = COALESCE(NULLIF(display_username, ""), username)', 
    'SELECT "No copy needed" as status');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SET @sql3 = IF(@username_exists > 0 AND @display_username_exists > 0, 
    'ALTER TABLE users DROP COLUMN username', 
    'SELECT "No drop needed" as status');
PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- If neither exists, create display_username
SET @sql4 = IF(@username_exists = 0 AND @display_username_exists = 0, 
    'ALTER TABLE users ADD COLUMN display_username VARCHAR(50) NOT NULL', 
    'SELECT "display_username already exists" as status');
PREPARE stmt4 FROM @sql4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;

-- Step 2: Ensure proper indexes and constraints exist
-- Drop existing indexes safely
DROP INDEX IF EXISTS username ON users;
DROP INDEX IF EXISTS idx_users_username ON users;

-- Create unique index on display_username
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_display_username ON users(display_username);

-- Step 3: Fix deposits table structure
-- Add missing columns if they don't exist
SET @plan_id_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                      WHERE TABLE_NAME = 'deposits' 
                      AND COLUMN_NAME = 'plan_id' 
                      AND TABLE_SCHEMA = DATABASE());

SET @sql5 = IF(@plan_id_exists = 0, 
    'ALTER TABLE deposits ADD COLUMN plan_id BIGINT', 
    'SELECT "plan_id already exists" as status');
PREPARE stmt5 FROM @sql5;
EXECUTE stmt5;
DEALLOCATE PREPARE stmt5;

SET @promo_code_id_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                            WHERE TABLE_NAME = 'deposits' 
                            AND COLUMN_NAME = 'promo_code_id' 
                            AND TABLE_SCHEMA = DATABASE());

SET @sql6 = IF(@promo_code_id_exists = 0, 
    'ALTER TABLE deposits ADD COLUMN promo_code_id BIGINT', 
    'SELECT "promo_code_id already exists" as status');
PREPARE stmt6 FROM @sql6;
EXECUTE stmt6;
DEALLOCATE PREPARE stmt6;

SET @bonus_amount_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                           WHERE TABLE_NAME = 'deposits' 
                           AND COLUMN_NAME = 'bonus_amount' 
                           AND TABLE_SCHEMA = DATABASE());

SET @sql7 = IF(@bonus_amount_exists = 0, 
    'ALTER TABLE deposits ADD COLUMN bonus_amount DECIMAL(19,2) DEFAULT 0.00', 
    'SELECT "bonus_amount already exists" as status');
PREPARE stmt7 FROM @sql7;
EXECUTE stmt7;
DEALLOCATE PREPARE stmt7;

SET @approved_at_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                          WHERE TABLE_NAME = 'deposits' 
                          AND COLUMN_NAME = 'approved_at' 
                          AND TABLE_SCHEMA = DATABASE());

SET @sql8 = IF(@approved_at_exists = 0, 
    'ALTER TABLE deposits ADD COLUMN approved_at DATETIME', 
    'SELECT "approved_at already exists" as status');
PREPARE stmt8 FROM @sql8;
EXECUTE stmt8;
DEALLOCATE PREPARE stmt8;

-- Step 4: Fix withdrawals table structure
SET @wallet_address_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                             WHERE TABLE_NAME = 'withdrawals' 
                             AND COLUMN_NAME = 'wallet_address' 
                             AND TABLE_SCHEMA = DATABASE());

SET @usdt_address_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                           WHERE TABLE_NAME = 'withdrawals' 
                           AND COLUMN_NAME = 'usdt_address' 
                           AND TABLE_SCHEMA = DATABASE());

-- If only usdt_address exists, rename it
SET @sql9 = IF(@usdt_address_exists > 0 AND @wallet_address_exists = 0, 
    'ALTER TABLE withdrawals CHANGE COLUMN usdt_address wallet_address VARCHAR(255) NOT NULL', 
    'SELECT "No rename needed - usdt_address to wallet_address" as status');
PREPARE stmt9 FROM @sql9;
EXECUTE stmt9;
DEALLOCATE PREPARE stmt9;

-- If both exist, keep wallet_address and drop usdt_address
SET @sql10 = IF(@usdt_address_exists > 0 AND @wallet_address_exists > 0, 
    'UPDATE withdrawals SET wallet_address = COALESCE(NULLIF(wallet_address, ""), usdt_address)', 
    'SELECT "No update needed" as status');
PREPARE stmt10 FROM @sql10;
EXECUTE stmt10;
DEALLOCATE PREPARE stmt10;

SET @sql11 = IF(@usdt_address_exists > 0 AND @wallet_address_exists > 0, 
    'ALTER TABLE withdrawals DROP COLUMN usdt_address', 
    'SELECT "No drop needed" as status');
PREPARE stmt11 FROM @sql11;
EXECUTE stmt11;
DEALLOCATE PREPARE stmt11;

-- Add rejection_note if missing
SET @rejection_note_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                             WHERE TABLE_NAME = 'withdrawals' 
                             AND COLUMN_NAME = 'rejection_note' 
                             AND TABLE_SCHEMA = DATABASE());

SET @sql12 = IF(@rejection_note_exists = 0, 
    'ALTER TABLE withdrawals ADD COLUMN rejection_note TEXT', 
    'SELECT "rejection_note already exists" as status');
PREPARE stmt12 FROM @sql12;
EXECUTE stmt12;
DEALLOCATE PREPARE stmt12;

-- Step 5: Fix promo_codes table column names
SET @bonus_value_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                          WHERE TABLE_NAME = 'promo_codes' 
                          AND COLUMN_NAME = 'bonus_value' 
                          AND TABLE_SCHEMA = DATABASE());

SET @bonus_amount_promo_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                                 WHERE TABLE_NAME = 'promo_codes' 
                                 AND COLUMN_NAME = 'bonus_amount' 
                                 AND TABLE_SCHEMA = DATABASE());

-- Rename bonus_amount to bonus_value if needed
SET @sql13 = IF(@bonus_amount_promo_exists > 0 AND @bonus_value_exists = 0, 
    'ALTER TABLE promo_codes CHANGE COLUMN bonus_amount bonus_value DECIMAL(19,2) NOT NULL', 
    'SELECT "No rename needed - bonus_amount to bonus_value" as status');
PREPARE stmt13 FROM @sql13;
EXECUTE stmt13;
DEALLOCATE PREPARE stmt13;

-- Fix max_uses to usage_limit
SET @usage_limit_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                          WHERE TABLE_NAME = 'promo_codes' 
                          AND COLUMN_NAME = 'usage_limit' 
                          AND TABLE_SCHEMA = DATABASE());

SET @max_uses_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                       WHERE TABLE_NAME = 'promo_codes' 
                       AND COLUMN_NAME = 'max_uses' 
                       AND TABLE_SCHEMA = DATABASE());

SET @sql14 = IF(@max_uses_exists > 0 AND @usage_limit_exists = 0, 
    'ALTER TABLE promo_codes CHANGE COLUMN max_uses usage_limit INT NOT NULL', 
    'SELECT "No rename needed - max_uses to usage_limit" as status');
PREPARE stmt14 FROM @sql14;
EXECUTE stmt14;
DEALLOCATE PREPARE stmt14;

-- Fix current_uses to used_count
SET @used_count_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                         WHERE TABLE_NAME = 'promo_codes' 
                         AND COLUMN_NAME = 'used_count' 
                         AND TABLE_SCHEMA = DATABASE());

SET @current_uses_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                           WHERE TABLE_NAME = 'promo_codes' 
                           AND COLUMN_NAME = 'current_uses' 
                           AND TABLE_SCHEMA = DATABASE());

SET @sql15 = IF(@current_uses_exists > 0 AND @used_count_exists = 0, 
    'ALTER TABLE promo_codes CHANGE COLUMN current_uses used_count INT NOT NULL DEFAULT 0', 
    'SELECT "No rename needed - current_uses to used_count" as status');
PREPARE stmt15 FROM @sql15;
EXECUTE stmt15;
DEALLOCATE PREPARE stmt15;

-- Step 6: Fix daily_counters table name if needed
SET @daily_counter_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
                            WHERE TABLE_NAME = 'daily_counter' 
                            AND TABLE_SCHEMA = DATABASE());

SET @daily_counters_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
                             WHERE TABLE_NAME = 'daily_counters' 
                             AND TABLE_SCHEMA = DATABASE());

SET @sql16 = IF(@daily_counter_exists > 0 AND @daily_counters_exists = 0, 
    'RENAME TABLE daily_counter TO daily_counters', 
    'SELECT "Table name already correct" as status');
PREPARE stmt16 FROM @sql16;
EXECUTE stmt16;
DEALLOCATE PREPARE stmt16;

-- Step 7: Create missing tables if they don't exist
CREATE TABLE IF NOT EXISTS referral_earnings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    referrer_id BIGINT NOT NULL,
    referred_user_id BIGINT NOT NULL,
    deposit_id BIGINT,
    amount DECIMAL(19,2) NOT NULL,
    commission_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transaction_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    transaction_id BIGINT,
    amount DECIMAL(19,2) NOT NULL,
    balance_before DECIMAL(19,2) NOT NULL,
    balance_after DECIMAL(19,2) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    admin_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Step 8: Re-enable foreign key checks and add foreign keys
SET foreign_key_checks = 1;

-- Add foreign key constraints safely
SET @sql17 = 'ALTER TABLE deposits ADD CONSTRAINT fk_deposits_plan FOREIGN KEY (plan_id) REFERENCES plans(id)';
SET @constraint_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
                         WHERE CONSTRAINT_NAME = 'fk_deposits_plan' 
                         AND TABLE_SCHEMA = DATABASE());
SET @sql17 = IF(@constraint_exists = 0, @sql17, 'SELECT "Constraint already exists" as status');
PREPARE stmt17 FROM @sql17;
EXECUTE stmt17;
DEALLOCATE PREPARE stmt17;

SET @sql18 = 'ALTER TABLE deposits ADD CONSTRAINT fk_deposits_promo_code FOREIGN KEY (promo_code_id) REFERENCES promo_codes(id)';
SET @constraint_exists2 = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
                          WHERE CONSTRAINT_NAME = 'fk_deposits_promo_code' 
                          AND TABLE_SCHEMA = DATABASE());
SET @sql18 = IF(@constraint_exists2 = 0, @sql18, 'SELECT "Constraint already exists" as status');
PREPARE stmt18 FROM @sql18;
EXECUTE stmt18;
DEALLOCATE PREPARE stmt18;

-- Step 9: Ensure admin user exists and is properly configured
INSERT IGNORE INTO users (
    full_name, 
    display_username, 
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
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
    0.00, 
    0.00, 
    0.00, 
    'ACTIVE', 
    'ADMIN', 
    NOW(), 
    NOW()
);

-- Update existing admin user to ensure proper setup
UPDATE users 
SET display_username = 'admin', 
    status = 'ACTIVE', 
    role = 'ADMIN',
    full_name = 'System Administrator'
WHERE phone_number = '1234567890';

-- Step 10: Verify the schema
SELECT 
    'Schema verification complete' as message,
    (SELECT COUNT(*) FROM users WHERE display_username = 'admin') as admin_users,
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'display_username') as display_username_exists,
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'username') as username_exists;