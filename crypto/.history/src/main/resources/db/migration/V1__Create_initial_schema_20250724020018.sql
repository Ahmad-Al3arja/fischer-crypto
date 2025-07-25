-- V1__Create_initial_schema.sql
-- Initial database schema for Investment Platform

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

-- Create main tables (without foreign keys first)
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
    updated_at DATETIME NOT NULL
);

CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    usdt_address VARCHAR(255) NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE deposits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_hash VARCHAR(255),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
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
    updated_at DATETIME NOT NULL
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
    created_at DATETIME NOT NULL
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
    updated_at DATETIME NOT NULL
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