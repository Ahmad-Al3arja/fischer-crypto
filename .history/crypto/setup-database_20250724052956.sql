-- Setup database manually if needed
-- Run this in MySQL/XAMPP phpMyAdmin

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS investment_platform;

-- Use the database
USE investment_platform;

-- Create plans table
CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    monthly_profit DECIMAL(19,2) NOT NULL,
    daily_profit_min DECIMAL(19,2) NOT NULL,
    daily_profit_max DECIMAL(19,2) NOT NULL,
    plan_level INT NOT NULL
);

-- Insert default plans
INSERT INTO plans (name, price, monthly_profit, daily_profit_min, daily_profit_max, plan_level) VALUES
('Starter', 100.00, 10.00, 0.30, 0.40, 1),
('Silver', 500.00, 60.00, 1.80, 2.20, 2),
('Gold', 1000.00, 130.00, 4.00, 4.80, 3),
('Platinum', 2500.00, 350.00, 10.50, 12.60, 4),
('Diamond', 5000.00, 750.00, 22.50, 27.00, 5);

-- Create admin user (password: admin123)
INSERT INTO users (full_name, display_username, phone_number, password, total_balance, frozen_balance, referral_earnings, status, role, created_at, updated_at) VALUES
('System Administrator', 'admin', '1234567890', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 0.00, 0.00, 0.00, 'ACTIVE', 'ADMIN', NOW(), NOW());

SELECT 'Database setup complete!' as message; 