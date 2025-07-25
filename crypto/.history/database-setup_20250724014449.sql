-- XAMPP MySQL Database Setup for Investment Platform
-- Run this script in phpMyAdmin or MySQL command line

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS investment_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE investment_platform;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS daily_counter;
DROP TABLE IF EXISTS referral_earnings;
DROP TABLE IF EXISTS promo_codes;
DROP TABLE IF EXISTS withdrawals;
DROP TABLE IF EXISTS deposits;
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS plans;
DROP TABLE IF EXISTS admin_settings;
DROP TABLE IF EXISTS commission_type;
DROP TABLE IF EXISTS withdrawal_status;
DROP TABLE IF EXISTS deposit_status;
DROP TABLE IF EXISTS user_status;
DROP TABLE IF EXISTS role;

-- Create enum tables
CREATE TABLE role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE deposit_status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE withdrawal_status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE commission_type (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Insert enum values
INSERT INTO role (name) VALUES ('USER'), ('ADMIN');
INSERT INTO user_status (name) VALUES ('ACTIVE'), ('INACTIVE'), ('SUSPENDED');
INSERT INTO deposit_status (name) VALUES ('PENDING'), ('APPROVED'), ('REJECTED');
INSERT INTO withdrawal_status (name) VALUES ('PENDING'), ('APPROVED'), ('REJECTED');
INSERT INTO commission_type (name) VALUES ('DIRECT'), ('SECOND_LEVEL');

-- Create main tables
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
    username VARCHAR(50) NOT NULL UNIQUE,
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
    FOREIGN KEY (current_plan_id) REFERENCES plans(id),
    FOREIGN KEY (referrer_id) REFERENCES users(id),
    FOREIGN KEY (grand_referrer_id) REFERENCES users(id)
);

CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    usdt_address VARCHAR(255) NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE deposits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_hash VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE withdrawals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    fee DECIMAL(19,2) NOT NULL,
    net_amount DECIMAL(19,2) NOT NULL,
    usdt_address VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE promo_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    bonus_amount DECIMAL(19,2) NOT NULL,
    max_uses INT NOT NULL,
    current_uses INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    expires_at DATETIME
);

CREATE TABLE referral_earnings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    referrer_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    commission_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (referrer_id) REFERENCES users(id)
);

CREATE TABLE daily_counter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    plan_day INT NOT NULL DEFAULT 1,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    current_day_profit DECIMAL(19,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
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
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_referrer ON users(referrer_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_deposits_user_status ON deposits(user_id, status);
CREATE INDEX idx_withdrawals_user_status ON withdrawals(user_id, status);
CREATE INDEX idx_daily_counter_user_active ON daily_counter(user_id, is_active);
CREATE INDEX idx_daily_counter_expired ON daily_counter(end_time, is_active);

-- Insert default data
INSERT INTO plans (name, price, monthly_profit, daily_profit_min, daily_profit_max, plan_level) VALUES
('Starter', 100.00, 10.00, 0.30, 0.40, 1),
('Silver', 500.00, 60.00, 1.80, 2.20, 2),
('Gold', 1000.00, 130.00, 4.00, 4.80, 3),
('Platinum', 2500.00, 350.00, 10.50, 12.60, 4),
('Diamond', 5000.00, 750.00, 22.50, 27.00, 5);

INSERT INTO admin_settings (key_name, value, description, created_at, updated_at) VALUES
('maintenance_mode', 'false', 'Platform maintenance mode', NOW(), NOW()),
('min_withdrawal', '10.00', 'Minimum withdrawal amount', NOW(), NOW()),
('max_withdrawal', '10000.00', 'Maximum withdrawal amount', NOW(), NOW()),
('withdrawal_fee', '2.00', 'Withdrawal fee percentage', NOW(), NOW()),
('referral_commission', '5.00', 'Referral commission percentage', NOW(), NOW()),
('auto_upgrade_enabled', 'true', 'Enable automatic plan upgrades', NOW(), NOW()),
('daily_profit_enabled', 'true', 'Enable daily profit calculations', NOW(), NOW()),
('platform_wallet', 'TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE', 'Platform USDT wallet address', NOW(), NOW());

-- Create admin user (password: admin123)
INSERT INTO users (full_name, username, phone_number, password, total_balance, frozen_balance, referral_earnings, status, role, created_at, updated_at) VALUES
('Admin User', 'admin', '1234567890', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 0.00, 0.00, 0.00, 'ACTIVE', 'ADMIN', NOW(), NOW());

SELECT 'Database setup completed successfully!' as status; 