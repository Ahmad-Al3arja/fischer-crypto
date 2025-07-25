-- V2__Insert_initial_data.sql
-- Insert initial data for Investment Platform

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
('referral_commission', '5.00', 'Referral commission percentage', NOW(), NOW()),
('auto_upgrade_enabled', 'true', 'Enable automatic plan upgrades', NOW(), NOW()),
('daily_profit_enabled', 'true', 'Enable daily profit calculations', NOW(), NOW()),
('platform_wallet', 'TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE', 'Platform USDT wallet address', NOW(), NOW());

-- Create admin user (password: admin123)
INSERT INTO users (full_name, display_username, phone_number, password, total_balance, frozen_balance, referral_earnings, status, role, created_at, updated_at) VALUES
('Admin User', 'admin', '1234567890', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 0.00, 0.00, 0.00, 'ACTIVE', 'ADMIN', NOW(), NOW()); 