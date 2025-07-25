-- V1__Complete_investment_platform_setup.sql
-- Complete database schema for Investment Platform - Single Migration File
-- Includes all tables, data, indexes, views, and features

SELECT 'Starting complete investment platform setup...' as status;

-- ============================================================================
-- CORE TABLES CREATION
-- ============================================================================

-- Create plans table with all required fields
CREATE TABLE plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    monthly_profit DECIMAL(19,2) NOT NULL,
    daily_profit_min DECIMAL(19,2) NOT NULL,
    daily_profit_max DECIMAL(19,2) NOT NULL,
    plan_level INT NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    display_username VARCHAR(50) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    total_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    frozen_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    referral_earnings DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    current_plan_id BIGINT,
    referrer_id BIGINT,
    grand_referrer_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    subscription_date DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (current_plan_id) REFERENCES plans(id),
    FOREIGN KEY (referrer_id) REFERENCES users(id),
    FOREIGN KEY (grand_referrer_id) REFERENCES users(id)
);

-- Create promo codes table
CREATE TABLE promo_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    bonus_value DECIMAL(19,2) NOT NULL,
    usage_limit INT NOT NULL DEFAULT 100,
    used_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME
);

-- Create deposits table
CREATE TABLE deposits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT,
    amount DECIMAL(19,2) NOT NULL,
    promo_code_id BIGINT,
    bonus_amount DECIMAL(19,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at DATETIME,
    processed_by BIGINT,
    processed_at DATETIME,
    notes TEXT,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (plan_id) REFERENCES plans(id),
    FOREIGN KEY (promo_code_id) REFERENCES promo_codes(id)
);

-- Create withdrawals table
CREATE TABLE withdrawals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    fee DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    net_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    wallet_address VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    processed_at DATETIME,
    processed_by BIGINT,
    notes TEXT,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create wallets table
CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    usdt_address VARCHAR(255) NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    address_set BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create wallet change requests table
CREATE TABLE wallet_change_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_address VARCHAR(255) NOT NULL,
    new_address VARCHAR(255) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by BIGINT,
    admin_notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (processed_by) REFERENCES users(id)
);

-- Create daily counters table
CREATE TABLE daily_counters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    plan_day INT NOT NULL DEFAULT 1,
    current_day_profit DECIMAL(19,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create referral earnings table
CREATE TABLE referral_earnings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    referrer_id BIGINT NOT NULL,
    referred_user_id BIGINT NOT NULL,
    deposit_id BIGINT,
    amount DECIMAL(19,2) NOT NULL,
    commission_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (referrer_id) REFERENCES users(id),
    FOREIGN KEY (referred_user_id) REFERENCES users(id),
    FOREIGN KEY (deposit_id) REFERENCES deposits(id)
);

-- Create referral usage tracking table
CREATE TABLE referral_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referrer_id BIGINT NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    usage_limit INT NOT NULL DEFAULT 100,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints and unique constraint
    FOREIGN KEY (referrer_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_referral_usage_referrer (referrer_id)
);

-- Create transaction logs table
CREATE TABLE transaction_logs (
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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create audit logs table
CREATE TABLE audit_logs (
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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

-- Create admin settings table
CREATE TABLE admin_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL UNIQUE,
    value TEXT NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create notifications table
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

-- Create user activity logs table
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

-- Create system settings table
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

-- Create user sessions table
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

SELECT 'Core tables created successfully!' as step1_status;

-- ============================================================================
-- INDEXES CREATION
-- ============================================================================

-- Users table indexes
CREATE INDEX idx_users_phone_number ON users(phone_number);
CREATE INDEX idx_users_display_username ON users(display_username);
CREATE INDEX idx_users_referrer ON users(referrer_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status_role ON users(status, role);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Plans table indexes
CREATE INDEX idx_plans_is_active ON plans(is_active);
CREATE INDEX idx_plans_plan_level ON plans(plan_level);

-- Deposits table indexes
CREATE INDEX idx_deposits_user_id ON deposits(user_id);
CREATE INDEX idx_deposits_status ON deposits(status);
CREATE INDEX idx_deposits_user_status ON deposits(user_id, status);
CREATE INDEX idx_deposits_created_at ON deposits(created_at);
CREATE INDEX idx_deposits_status_created_at ON deposits(status, created_at);

-- Withdrawals table indexes
CREATE INDEX idx_withdrawals_user_id ON withdrawals(user_id);
CREATE INDEX idx_withdrawals_status ON withdrawals(status);
CREATE INDEX idx_withdrawals_user_status ON withdrawals(user_id, status);
CREATE INDEX idx_withdrawals_created_at ON withdrawals(created_at);
CREATE INDEX idx_withdrawals_status_created_at ON withdrawals(status, created_at);

-- Daily counters table indexes
CREATE INDEX idx_daily_counters_user_id ON daily_counters(user_id);
CREATE INDEX idx_daily_counters_is_active ON daily_counters(is_active);
CREATE INDEX idx_daily_counters_end_time ON daily_counters(end_time);

-- Referral earnings table indexes
CREATE INDEX idx_referral_earnings_referrer_id ON referral_earnings(referrer_id);
CREATE INDEX idx_referral_earnings_user_id ON referral_earnings(user_id);
CREATE INDEX idx_referral_earnings_created_at ON referral_earnings(created_at);
CREATE INDEX idx_referral_earnings_referrer_created_at ON referral_earnings(referrer_id, created_at);

-- Referral usage table indexes
CREATE INDEX idx_referral_usage_active ON referral_usage(is_active);
CREATE INDEX idx_referral_usage_limit ON referral_usage(usage_count, usage_limit);

-- Transaction logs table indexes
CREATE INDEX idx_transaction_logs_user_id ON transaction_logs(user_id);
CREATE INDEX idx_transaction_logs_type ON transaction_logs(transaction_type);
CREATE INDEX idx_transaction_logs_created_at ON transaction_logs(created_at);

-- Audit logs table indexes
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_admin_id ON audit_logs(admin_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- Promo codes table indexes
CREATE INDEX idx_promo_codes_code ON promo_codes(code);
CREATE INDEX idx_promo_codes_is_active ON promo_codes(is_active);

-- Wallet change requests indexes
CREATE INDEX idx_wallet_change_requests_user_id ON wallet_change_requests(user_id);
CREATE INDEX idx_wallet_change_requests_status ON wallet_change_requests(status);
CREATE INDEX idx_wallet_change_requests_created_at ON wallet_change_requests(created_at);

-- Notifications indexes
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_type ON notifications(type);

-- User activity logs indexes
CREATE INDEX idx_user_activity_logs_user_id ON user_activity_logs(user_id);
CREATE INDEX idx_user_activity_logs_activity_type ON user_activity_logs(activity_type);
CREATE INDEX idx_user_activity_logs_created_at ON user_activity_logs(created_at);

-- System settings indexes
CREATE INDEX idx_system_settings_category ON system_settings(category);
CREATE INDEX idx_system_settings_key_name ON system_settings(key_name);

-- Password reset tokens indexes
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);

-- User sessions indexes
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX idx_user_sessions_is_active ON user_sessions(is_active);

-- Add unique constraint to wallet addresses
ALTER TABLE wallets ADD CONSTRAINT uk_wallets_usdt_address UNIQUE (usdt_address);

SELECT 'Indexes created successfully!' as step2_status;

-- ============================================================================
-- INSERT THE 9 CORRECT INVESTMENT PLANS
-- ============================================================================

INSERT INTO plans (name, price, monthly_profit, daily_profit_min, daily_profit_max, plan_level, is_active, created_at, updated_at) VALUES
-- المستوى الأول (60 دولار): من 1.2$ إلى 1.8$ يوميًا
('المستوى الأول', 60.00, 45.00, 1.20, 1.80, 1, true, NOW(), NOW()),

-- المستوى الثاني (150 دولار): من 2.4$ إلى 4.9$ يوميًا  
('المستوى الثاني', 150.00, 110.00, 2.40, 4.90, 2, true, NOW(), NOW()),

-- المستوى الثالث (300 دولار): من 4.9$ إلى 6.9$ يوميًا
('المستوى الثالث', 300.00, 175.00, 4.90, 6.90, 3, true, NOW(), NOW()),

-- المستوى الرابع (500 دولار): من 6.9$ إلى 9.9$ يوميًا
('المستوى الرابع', 500.00, 245.00, 6.90, 9.90, 4, true, NOW(), NOW()),

-- المستوى الخامس (800 دولار): من 11.9$ إلى 22$ يوميًا
('المستوى الخامس', 800.00, 505.00, 11.90, 22.00, 5, true, NOW(), NOW()),

-- المستوى السادس (1500 دولار): من 23$ إلى 45$ يوميًا
('المستوى السادس', 1500.00, 1020.00, 23.00, 45.00, 6, true, NOW(), NOW()),

-- المستوى السابع (3000 دولار): من 45$ إلى 63.3$ يوميًا
('المستوى السابع', 3000.00, 1624.50, 45.00, 63.30, 7, true, NOW(), NOW()),

-- المستوى الثامن (5000 دولار): من 63.3$ إلى 95$ يوميًا
('المستوى الثامن', 5000.00, 2374.50, 63.30, 95.00, 8, true, NOW(), NOW()),

-- المستوى التاسع (8000 دولار): من 125$ إلى 223.3$ يوميًا
('المستوى التاسع', 8000.00, 5224.50, 125.00, 223.30, 9, true, NOW(), NOW());

SELECT '9 Investment plans inserted successfully!' as step3_status;

-- ============================================================================
-- INSERT ADMIN SETTINGS
-- ============================================================================

INSERT INTO admin_settings (key_name, value, description, created_at, updated_at) VALUES
('maintenance_mode', 'false', 'Platform maintenance mode', NOW(), NOW()),
('min_withdrawal', '10.00', 'Minimum withdrawal amount', NOW(), NOW()),
('max_withdrawal', '10000.00', 'Maximum withdrawal amount', NOW(), NOW()),
('withdrawal_fee', '2.00', 'Withdrawal fee percentage', NOW(), NOW()),
('referral_commission_direct', '12.00', 'Direct referral commission percentage', NOW(), NOW()),
('referral_commission_grand', '6.00', 'Grand referral commission percentage', NOW(), NOW()),
('auto_upgrade_enabled', 'true', 'Enable automatic plan upgrades', NOW(), NOW()),
('daily_profit_enabled', 'true', 'Enable daily profit calculations', NOW(), NOW()),
('platform_wallet', 'TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE', 'Platform USDT wallet address', NOW(), NOW()),
('about_content', 'Welcome to our investment platform! Start your journey with us today.', 'About platform content', NOW(), NOW()),
('default_promo_usage_limit', '100', 'Default usage limit for new promo codes', NOW(), NOW()),
('default_referral_limit', '100', 'Default referral usage limit for new users', NOW(), NOW()),
('referral_system_enabled', 'true', 'Enable or disable referral system', NOW(), NOW()),
('max_referral_limit', '1000', 'Maximum referral limit that can be set', NOW(), NOW()),
('wallet_change_requests_enabled', 'true', 'Allow users to request wallet address changes', NOW(), NOW()),
('auto_approve_small_withdrawals', 'false', 'Auto-approve withdrawals under a certain amount', NOW(), NOW()),
('small_withdrawal_threshold', '100.00', 'Threshold for auto-approval of small withdrawals', NOW(), NOW()),
('referral_bonus_enabled', 'true', 'Enable referral bonus system', NOW(), NOW()),
('daily_profit_auto_calculation', 'true', 'Automatically calculate daily profits', NOW(), NOW()),
('referral_default_limit', '100', 'Default referral usage limit for new users', NOW(), NOW()),
('referral_max_limit', '10000', 'Maximum referral usage limit that can be set', NOW(), NOW()),
('wallet_change_enabled', 'true', 'Allow users to request wallet address changes', NOW(), NOW()),
('wallet_one_time_set', 'true', 'Wallet addresses can only be set once without admin approval', NOW(), NOW());

SELECT 'Admin settings inserted successfully!' as step4_status;

-- ============================================================================
-- INSERT SYSTEM SETTINGS
-- ============================================================================

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

SELECT 'System settings inserted successfully!' as step5_status;

-- ============================================================================
-- CREATE ADMIN USER
-- ============================================================================

-- Create admin user (password: admin123)
INSERT INTO users (
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

SELECT 'Admin user created successfully!' as step6_status;

-- ============================================================================
-- INSERT SAMPLE PROMO CODES
-- ============================================================================

INSERT INTO promo_codes (code, bonus_value, usage_limit, used_count, is_active, created_at, expires_at) VALUES
('WELCOME50', 50.00, 100, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
('BONUS100', 100.00, 50, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY)),
('VIP200', 200.00, 20, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 120 DAY)),
('STARTER25', 25.00, 200, 0, true, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

SELECT 'Sample promo codes inserted successfully!' as step7_status;

-- ============================================================================
-- CREATE VIEWS FOR REPORTING
-- ============================================================================

-- User statistics view
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

-- Plan statistics view
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

-- Referral statistics view
CREATE VIEW referral_statistics AS
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

SELECT 'Views created successfully!' as step8_status;

-- ============================================================================
-- FINAL VERIFICATION
-- ============================================================================

SELECT 'Complete Investment Platform Setup Finished Successfully!' as final_message;

-- Show the new 9 investment plans
SELECT 
    'Investment Plans Created:' as info,
    id,
    name as plan_name,
    CONCAT('$', FORMAT(price, 2)) as price,
    CONCAT('$', FORMAT(daily_profit_min, 2), ' - $', FORMAT(daily_profit_max, 2)) as daily_profit_range,
    CONCAT('$', FORMAT(monthly_profit, 2)) as estimated_monthly,
    plan_level
FROM plans 
ORDER BY plan_level;

-- Show admin user
SELECT 
    'Admin User Created:' as info,
    display_username as username,
    phone_number,
    status,
    role
FROM users 
WHERE role = 'ADMIN';

-- Show database summary
SELECT 
    'Database Summary:' as info,
    (SELECT COUNT(*) FROM plans) as total_plans,
    (SELECT COUNT(*) FROM users WHERE role = 'ADMIN') as total_admins,
    (SELECT COUNT(*) FROM admin_settings) as admin_settings_count,
    (SELECT COUNT(*) FROM system_settings) as system_settings_count,
    (SELECT COUNT(*) FROM promo_codes) as promo_codes_count;

-- Show table counts
SELECT 
    'Tables Created Successfully:' as summary,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE') as total_tables,
    (SELECT COUNT(*) FROM information_schema.views WHERE table_schema = DATABASE()) as total_views;

SELECT 'Platform is ready for production use!' as success_message;
SELECT 'Default Admin Login: Phone: 1234567890, Password: admin123' as admin_credentials;