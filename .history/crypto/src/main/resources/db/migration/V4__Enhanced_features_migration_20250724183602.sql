-- V4__Enhanced_features_migration.sql
-- Add support for wallet change requests, enhanced referral tracking, and other improvements

-- Create wallet change requests table
CREATE TABLE wallet_change_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_address VARCHAR(255) NOT NULL,
    new_address VARCHAR(255) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at DATETIME NOT NULL,
    processed_at DATETIME,
    processed_by BIGINT,
    rejection_reason TEXT,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (processed_by) REFERENCES users(id)
);

-- Add indexes for wallet change requests
CREATE INDEX idx_wallet_change_requests_user_id ON wallet_change_requests(user_id);
CREATE INDEX idx_wallet_change_requests_status ON wallet_change_requests(status);
CREATE INDEX idx_wallet_change_requests_requested_at ON wallet_change_requests(requested_at);

-- Add unique constraint to wallet addresses to prevent duplicates
ALTER TABLE wallets ADD CONSTRAINT uk_wallets_usdt_address UNIQUE (usdt_address);

-- Enhance plans table with additional fields
ALTER TABLE plans ADD COLUMN description TEXT;
ALTER TABLE plans ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE plans ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE plans ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Update existing plans to have timestamps
UPDATE plans SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL;

-- Create notification table for system notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add indexes for notifications
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_type ON notifications(type);

-- Update referral_usage table structure (if not already created by V3)
CREATE TABLE IF NOT EXISTS referral_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referrer_id BIGINT NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    usage_limit INT NOT NULL DEFAULT 100,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (referrer_id) REFERENCES users(id),
    UNIQUE KEY uk_referral_usage_referrer (referrer_id)
);

-- Create indexes for referral_usage if not exists
CREATE INDEX IF NOT EXISTS idx_referral_usage_active ON referral_usage(is_active);
CREATE INDEX IF NOT EXISTS idx_referral_usage_limit ON referral_usage(usage_count, usage_limit);

-- Insert or update referral usage records for existing users
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

-- Create user activity log table
CREATE TABLE user_activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(100) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    metadata JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add indexes for user activity logs
CREATE INDEX idx_user_activity_logs_user_id ON user_activity_logs(user_id);
CREATE INDEX idx_user_activity_logs_activity_type ON user_activity_logs(activity_type);
CREATE INDEX idx_user_activity_logs_created_at ON user_activity_logs(created_at);

-- Create system settings table for more flexible configuration
CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    key_name VARCHAR(100) NOT NULL,
    value TEXT NOT NULL,
    data_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    description TEXT,
    is_editable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_system_settings_category_key (category, key_name)
);

-- Add indexes for system settings
CREATE INDEX idx_system_settings_category ON system_settings(category);
CREATE INDEX idx_system_settings_key_name ON system_settings(key_name);

-- Insert enhanced system settings
INSERT INTO system_settings (category, key_name, value, data_type, description, is_editable) VALUES
-- Referral settings
('REFERRAL', 'default_usage_limit', '100', 'INTEGER', 'Default referral usage limit for new users', TRUE),
('REFERRAL', 'max_usage_limit', '10000', 'INTEGER', 'Maximum referral usage limit that can be set', TRUE),
('REFERRAL', 'direct_commission_rate', '0.12', 'DECIMAL', 'Direct referral commission rate (12%)', TRUE),
('REFERRAL', 'grand_commission_rate', '0.06', 'DECIMAL', 'Grand referral commission rate (6%)', TRUE),

-- Withdrawal settings
('WITHDRAWAL', 'min_amount', '10.00', 'DECIMAL', 'Minimum withdrawal amount', TRUE),
('WITHDRAWAL', 'max_amount', '50000.00', 'DECIMAL', 'Maximum withdrawal amount', TRUE),
('WITHDRAWAL', 'fee_percentage', '2.00', 'DECIMAL', 'Withdrawal fee percentage', TRUE),
('WITHDRAWAL', 'daily_limit', '10000.00', 'DECIMAL', 'Daily withdrawal limit per user', TRUE),

-- Platform settings
('PLATFORM', 'maintenance_mode', 'false', 'BOOLEAN', 'Platform maintenance mode', TRUE),
('PLATFORM', 'registration_enabled', 'true', 'BOOLEAN', 'Allow new user registrations', TRUE),
('PLATFORM', 'usdt_wallet_address', 'TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE', 'STRING', 'Platform USDT wallet address', TRUE),

-- Security settings
('SECURITY', 'max_login_attempts', '5', 'INTEGER', 'Maximum login attempts before lockout', TRUE),
('SECURITY', 'lockout_duration_minutes', '30', 'INTEGER', 'Account lockout duration in minutes', TRUE),
('SECURITY', 'password_min_length', '6', 'INTEGER', 'Minimum password length', FALSE),

-- Notification settings
('NOTIFICATION', 'email_notifications', 'true', 'BOOLEAN', 'Enable email notifications', TRUE),
('NOTIFICATION', 'sms_notifications', 'false', 'BOOLEAN', 'Enable SMS notifications', TRUE),
('NOTIFICATION', 'admin_email', 'admin@yourapp.com', 'STRING', 'Admin notification email', TRUE);

-- Create password reset tokens table
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    used_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add indexes for password reset tokens
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);

-- Create user sessions table for better session management
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at DATETIME NOT NULL,
    last_activity DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add indexes for user sessions
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX idx_user_sessions_is_active ON user_sessions(is_active);

-- Add some useful views for reporting
CREATE VIEW user_statistics AS
SELECT 
    u.id,
    u.display_username,
    u.full_name,
    u.phone_number,
    u.status,
    u.current_plan_id,
    p.name as plan_name,
    u.total_balance,
    u.frozen_balance,
    u.referral_earnings,
    (SELECT COUNT(*) FROM users r WHERE r.referrer_id = u.id) as direct_referrals,
    (SELECT COUNT(*) FROM users r WHERE r.grand_referrer_id = u.id) as second_level_referrals,
    (SELECT COUNT(*) FROM deposits d WHERE d.user_id = u.id AND d.status = 'APPROVED') as total_deposits,
    (SELECT COALESCE(SUM(d.amount), 0) FROM deposits d WHERE d.user_id = u.id AND d.status = 'APPROVED') as total_deposited,
    (SELECT COUNT(*) FROM withdrawals w WHERE w.user_id = u.id AND w.status = 'APPROVED') as total_withdrawals,
    (SELECT COALESCE(SUM(w.amount), 0) FROM withdrawals w WHERE w.user_id = u.id AND w.status = 'APPROVED') as total_withdrawn,
    u.created_at,
    u.subscription_date
FROM users u
LEFT JOIN plans p ON u.current_plan_id = p.id
WHERE u.role = 'USER';

CREATE VIEW plan_statistics AS
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

-- Update admin settings with new categories
INSERT INTO admin_settings (key_name, value, description, created_at, updated_at) VALUES
('wallet_change_requests_enabled', 'true', 'Allow users to request wallet address changes', NOW(), NOW()),
('auto_approve_small_withdrawals', 'false', 'Auto-approve withdrawals under a certain amount', NOW(), NOW()),
('small_withdrawal_threshold', '100.00', 'Threshold for auto-approval of small withdrawals', NOW(), NOW()),
('referral_bonus_enabled', 'true', 'Enable referral bonus system', NOW(), NOW()),
('daily_profit_auto_calculation', 'true', 'Automatically calculate daily profits', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    updated_at = NOW();

-- Create indexes for better performance on frequently queried tables
CREATE INDEX idx_users_status_role ON users(status, role);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_deposits_status_created_at ON deposits(status, created_at);
CREATE INDEX idx_withdrawals_status_created_at ON withdrawals(status, created_at);
CREATE INDEX idx_referral_earnings_referrer_created_at ON referral_earnings(referrer_id, created_at);

-- Add some sample notifications for admin
INSERT INTO notifications (user_id, title, message, type) 
SELECT 
    u.id,
    'Welcome to the Platform!',
    'Thank you for joining our investment platform. Start by exploring our investment plans and making your first deposit.',
    'WELCOME'
FROM users u 
WHERE u.role = 'USER' AND u.created_at > DATE_SUB(NOW(), INTERVAL 7 DAY)
LIMIT 10;

-- Verify the migration
SELECT 'Enhanced features migration completed successfully!' as message;

SELECT 
    'Tables created:' as info,
    COUNT(*) as total_tables
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
    AND table_name IN ('wallet_change_requests', 'notifications', 'user_activity_logs', 'system_settings', 'password_reset_tokens', 'user_sessions');

SELECT 
    'New system settings:' as info,
    COUNT(*) as total_settings
FROM system_settings;

SELECT 
    'Views created:' as info,
    COUNT(*) as total_views
FROM information_schema.views 
WHERE table_schema = DATABASE() 
    AND table_name IN ('user_statistics', 'plan_statistics');