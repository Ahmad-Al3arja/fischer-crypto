-- Initialize default plans
INSERT INTO plans (name, price, monthly_profit, daily_profit_min, daily_profit_max, plan_level) VALUES
('Starter', 100.00, 10.00, 0.30, 0.40, 1),
('Silver', 500.00, 60.00, 1.80, 2.20, 2),
('Gold', 1000.00, 130.00, 4.00, 4.80, 3),
('Platinum', 2500.00, 350.00, 10.50, 12.60, 4),
('Diamond', 5000.00, 750.00, 22.50, 27.00, 5);

-- Initialize admin settings
INSERT INTO admin_settings (key_name, value, description) VALUES
('maintenance_mode', 'false', 'Platform maintenance mode'),
('min_withdrawal', '10.00', 'Minimum withdrawal amount'),
('max_withdrawal', '10000.00', 'Maximum withdrawal amount'),
('withdrawal_fee', '2.00', 'Withdrawal fee percentage'),
('referral_commission', '5.00', 'Referral commission percentage'),
('auto_upgrade_enabled', 'true', 'Enable automatic plan upgrades'),
('daily_profit_enabled', 'true', 'Enable daily profit distribution');

-- Initialize daily counter for today
INSERT INTO daily_counter (date, total_users, total_deposits, total_withdrawals, total_profit_distributed) VALUES
(CURDATE(), 0, 0, 0, 0.00); 