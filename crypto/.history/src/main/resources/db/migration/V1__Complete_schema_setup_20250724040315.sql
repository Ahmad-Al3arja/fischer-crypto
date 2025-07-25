-- V1__Complete_schema_setup.sql
-- Complete database schema for Investment Platform
-- This replaces all previous migration files

-- Create all tables with correct structure
CREATE TABLE plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    monthly_profit DECIMAL(19,2) NOT NULL,
    daily_profit_min DECIMAL(19,2) NOT NULL,
    daily_profit_max DECIMAL(19,2) NOT NULL,
    plan_level INT NOT NULL
);

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
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    
    -- Add foreign key constraints
    FOREIGN KEY (current_plan_id) REFERENCES plans(id),
    FOREIGN KEY (referrer_id) REFERENCES users(id),
    FOREIGN KEY (grand_referrer_id) REFERENCES users(id)
);

CREATE TABLE promo_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    bonus_value DECIMAL(19,2) NOT NULL,
    usage_limit INT NOT NULL,
    used_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    expires_at DATETIME
);

CREATE TABLE deposits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT,
    amount DECIMAL(19,2) NOT NULL,
    promo_code_id BIGINT,
    bonus_amount DECIMAL(19,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    approved_at DATETIME,
    processed_by BIGINT,
    processed_at DATETIME,
    notes TEXT,
    
    -- Add foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (plan_id) REFERENCES plans(id),
    FOREIGN KEY (promo_code_id) REFERENCES promo_codes(id)
);

CREATE TABLE withdrawals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    fee DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    net_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    wallet_address VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_note TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    processed_at DATETIME,
    processed_by BIGINT,
    notes TEXT,
    
    -- Add foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    usdt_address VARCHAR(255) NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    
    -- Add foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE daily_counters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    plan_day INT NOT NULL DEFAULT 1,
    current_day_profit DECIMAL(19,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Add foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE referral_earnings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    referrer_id BIGINT NOT NULL,
    referred_user_id BIGINT NOT NULL,
    deposit_id BIGINT,
    amount DECIMAL(19,2) NOT NULL,
    commission_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Add foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (referrer_id) REFERENCES users(id),
    FOREIGN KEY (referred_user_id) REFERENCES users(id),
    FOREIGN KEY (deposit_id) REFERENCES deposits(id)
);

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
    
    -- Add foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id)
);

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
    
    -- Add foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (admin_id) REFERENCES users(id)
);

CREATE TABLE admin_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL UNIQUE,
    value TEXT NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- Create indexes for better performance
CREATE INDEX idx_users_phone_number ON users(phone_number);
CREATE INDEX idx_users_display_username ON users(display_username);
CREATE INDEX idx_users_referrer ON users(referrer_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);

CREATE INDEX idx_deposits_user_id ON deposits(user_id);
CREATE INDEX idx_deposits_status ON deposits(status);
CREATE INDEX idx_deposits_user_status ON deposits(user_id, status);
CREATE INDEX idx_deposits_created_at ON deposits(created_at);

CREATE INDEX idx_withdrawals_user_id ON withdrawals(user_id);
CREATE INDEX idx_withdrawals_status ON withdrawals(status);
CREATE INDEX idx_withdrawals_user_status ON withdrawals(user_id, status);
CREATE INDEX idx_withdrawals_created_at ON withdrawals(created_at);

CREATE INDEX idx_daily_counters_user_id ON daily_counters(user_id);
CREATE INDEX idx_daily_counters_is_active ON daily_counters(is_active);
CREATE INDEX idx_daily_counters_end_time ON daily_counters(end_time);

CREATE INDEX idx_referral_earnings_referrer_id ON referral_earnings(referrer_id);
CREATE INDEX idx_referral_earnings_user_id ON referral_earnings(user_id);
CREATE INDEX idx_referral_earnings_created_at ON referral_earnings(created_at);

CREATE INDEX idx_transaction_logs_user_id ON transaction_logs(user_id);
CREATE INDEX idx_transaction_logs_type ON transaction_logs(transaction_type);
CREATE INDEX idx_transaction_logs_created_at ON transaction_logs(created_at);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_admin_id ON audit_logs(admin_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

CREATE INDEX idx_promo_codes_code ON promo_codes(code);
CREATE INDEX idx_promo_codes_is_active ON promo_codes(is_active);

-- Insert default plans
INSERT INTO plans (name, price, monthly_profit, daily_profit_min, daily_profit_max, plan_level) VALUES
('Starter', 100.00, 10.00, 0.30, 0.40, 1),
('Silver', 500.00, 60.00, 1.80, 2.20, 2),
('Gold', 1000.00, 130.00, 4.00, 4.80, 3),
('Platinum', 2500.00, 350.00, 10.50, 12.60, 4),
('Diamond', 5000.00, 750.00, 22.50, 27.00, 5);

-- Insert admin settings
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
('about_content', 'Welcome to our investment platform! Start your journey with us today.', 'About platform content', NOW(), NOW());

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

-- Verify the setup
SELECT 'Database setup complete!' as message;
SELECT 
    'Admin user created:' as info,
    display_username as username,
    phone_number,
    status,
    role
FROM users 
WHERE role = 'ADMIN';

SELECT 
    'Plans created:' as info,
    COUNT(*) as total_plans
FROM plans;

SELECT 
    'Settings configured:' as info,
    COUNT(*) as total_settings
FROM admin_settings;