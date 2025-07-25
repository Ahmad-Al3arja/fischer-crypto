-- V5__Add_missing_fields_migration.sql
-- Add missing fields to plans table and ensure all tables are properly configured

-- Add missing columns to plans table if they don't exist
ALTER TABLE plans ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE plans ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE plans ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE plans ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Update existing plans to have timestamps if they're null
UPDATE plans SET 
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL OR updated_at IS NULL;

-- Create referral_usage table if it doesn't exist
CREATE TABLE IF NOT EXISTS referral_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referrer_id BIGINT NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    usage_limit INT NOT NULL DEFAULT 100,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_referral_usage_referrer (referrer_id),
    FOREIGN KEY (referrer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create wallet_change_requests table if it doesn't exist
CREATE TABLE IF NOT EXISTS wallet_change_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_address VARCHAR(255) NOT NULL,
    new_address VARCHAR(255) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME,
    processed_by BIGINT,
    rejection_reason TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_referral_usage_active ON referral_usage(is_active);
CREATE INDEX IF NOT EXISTS idx_referral_usage_limit ON referral_usage(usage_count, usage_limit);
CREATE INDEX IF NOT EXISTS idx_wallet_change_requests_user_id ON wallet_change_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_wallet_change_requests_status ON wallet_change_requests(status);
CREATE INDEX IF NOT EXISTS idx_wallet_change_requests_requested_at ON wallet_change_requests(requested_at);
CREATE INDEX IF NOT EXISTS idx_plans_is_active ON plans(is_active);
CREATE INDEX IF NOT EXISTS idx_plans_plan_level ON plans(plan_level);

-- Insert or update referral usage records for all existing users
INSERT INTO referral_usage (referrer_id, usage_count, usage_limit, is_active, created_at, updated_at)
SELECT 
    u.id,
    COALESCE((SELECT COUNT(*) FROM users ref WHERE ref.referrer_id = u.id), 0) as current_usage,
    100 as default_limit,
    TRUE as is_active,
    NOW(),
    NOW()
FROM users u 
WHERE u.role = 'USER'
ON DUPLICATE KEY UPDATE 
    usage_count = COALESCE((SELECT COUNT(*) FROM users ref WHERE ref.referrer_id = referrer_id), 0),
    updated_at = NOW();

-- Add unique constraint to wallet addresses if not exists
ALTER TABLE wallets ADD CONSTRAINT uk_wallets_usdt_address UNIQUE (usdt_address);

-- Update system settings with enhanced referral configuration
INSERT INTO admin_settings (key_name, value, description, created_at, updated_at) VALUES
('referral_default_limit', '100', 'Default referral usage limit for new users', NOW(), NOW()),
('referral_max_limit', '10000', 'Maximum referral usage limit that can be set', NOW(), NOW()),
('wallet_change_enabled', 'true', 'Allow users to request wallet address changes', NOW(), NOW()),
('wallet_one_time_set', 'true', 'Wallet addresses can only be set once without admin approval', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Create a view for referral statistics
CREATE OR REPLACE VIEW referral_statistics AS
SELECT 
    u.id as user_id,
    u.display_username,
    u.full_name,
    COALESCE(ru.usage_count, 0) as usage_count,
    COALESCE(ru.usage_limit, 100) as usage_limit,
    COALESCE(ru.is_active, TRUE) as is_active,
    (SELECT COUNT(*) FROM users ref WHERE ref.referrer_id = u.id) as direct_referrals,
    (SELECT COUNT(*) FROM users ref WHERE ref.grand_referrer_id = u.id) as second_level_referrals,
    u.referral_earnings,
    CASE 
        WHEN ru.usage_limit > 0 THEN ROUND((ru.usage_count / ru.usage_limit) * 100, 2)
        ELSE 0
    END as usage_percentage
FROM users u
LEFT JOIN referral_usage ru ON u.id = ru.referrer_id
WHERE u.role = 'USER';

-- Create a view for plan statistics
CREATE OR REPLACE VIEW plan_statistics_enhanced AS
SELECT 
    p.id,
    p.name,
    p.price,
    p.plan_level,
    p.is_active,
    (SELECT COUNT(*) FROM users u WHERE u.current_plan_id = p.id) as total_users,
    (SELECT COALESCE(SUM(u.total_balance), 0) FROM users u WHERE u.current_plan_id = p.id) as total_user_balance,
    (SELECT COALESCE(SUM(u.frozen_balance), 0) FROM users u WHERE u.current_plan_id = p.id) as total_frozen_balance,
    (SELECT COUNT(*) FROM deposits d JOIN users u ON d.user_id = u.id WHERE u.current_plan_id = p.id AND d.status = 'APPROVED') as total_deposits,
    (SELECT COALESCE(SUM(d.amount), 0) FROM deposits d JOIN users u ON d.user_id = u.id WHERE u.current_plan_id = p.id AND d.status = 'APPROVED') as total_deposited,
    p.created_at
FROM plans p;

-- Verify the migration
SELECT 'V5 Migration completed successfully!' as message;

SELECT 
    'Referral usage records created:' as info, 
    COUNT(*) as total_records 
FROM referral_usage;

SELECT 
    'Plans with missing fields updated:' as info, 
    COUNT(*) as total_plans 
FROM plans 
WHERE created_at IS NOT NULL AND updated_at IS NOT NULL;

-- Show referral statistics
SELECT 
    'Referral statistics summary:' as info,
    COUNT(*) as total_users_with_referral_tracking,
    AVG(usage_limit) as average_usage_limit,
    SUM(usage_count) as total_referrals_used
FROM referral_usage;