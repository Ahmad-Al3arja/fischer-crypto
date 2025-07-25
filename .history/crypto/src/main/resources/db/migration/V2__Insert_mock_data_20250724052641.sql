-- V2__Insert_mock_data.sql
-- Insert comprehensive mock data for testing

-- Insert additional promo codes
INSERT INTO promo_codes (code, bonus_value, usage_limit, used_count, is_active, created_at, expires_at) VALUES
('WELCOME50', 50.00, 100, 25, true, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
('BONUS100', 100.00, 50, 12, true, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY)),
('VIP200', 200.00, 20, 5, true, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
('EXPIRED10', 10.00, 10, 10, false, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Insert test users with different scenarios
INSERT INTO users (
    full_name, 
    display_username, 
    phone_number, 
    password, 
    total_balance, 
    frozen_balance, 
    referral_earnings, 
    current_plan_id,
    status, 
    role, 
    subscription_date,
    created_at, 
    updated_at
) VALUES
-- Test user 1: Active user with Silver plan
('John Smith', 'johnsmith', '+1234567890', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1250.50, 500.00, 150.00, 2, 'ACTIVE', 'USER', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),

-- Test user 2: Active user with Gold plan
('Sarah Johnson', 'sarahj', '+1987654321', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 2800.75, 1000.00, 300.00, 3, 'ACTIVE', 'USER', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),

-- Test user 3: Inactive user
('Mike Wilson', 'mikew', '+1555123456', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 0.00, 0.00, 0.00, NULL, 'INACTIVE', 'USER', NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),

-- Test user 4: User with Platinum plan
('Emma Davis', 'emmad', '+1444333222', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 5200.00, 2500.00, 500.00, 4, 'ACTIVE', 'USER', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),

-- Test user 5: User with Diamond plan
('Alex Brown', 'alexb', '+1777888999', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 12000.00, 5000.00, 1200.00, 5, 'ACTIVE', 'USER', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),

-- Test user 6: User with Starter plan
('Lisa Chen', 'lisac', '+1666777888', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 150.00, 100.00, 25.00, 1, 'ACTIVE', 'USER', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 35 DAY), NOW());

-- Set up referral relationships
UPDATE users SET referrer_id = 1 WHERE id = 2; -- Sarah referred by John
UPDATE users SET referrer_id = 1, grand_referrer_id = 1 WHERE id = 3; -- Mike referred by John
UPDATE users SET referrer_id = 2 WHERE id = 4; -- Emma referred by Sarah
UPDATE users SET referrer_id = 4 WHERE id = 5; -- Alex referred by Emma
UPDATE users SET referrer_id = 1 WHERE id = 6; -- Lisa referred by John

-- Insert wallets for active users
INSERT INTO wallets (user_id, usdt_address, is_locked, created_at, updated_at) VALUES
(1, 'TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE', false, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
(2, 'TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t', false, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),
(4, 'TXk8rQqJqJqJqJqJqJqJqJqJqJqJqJqJqJq', false, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
(5, 'TYk8rQqJqJqJqJqJqJqJqJqJqJqJqJqJqJq', false, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
(6, 'TZk8rQqJqJqJqJqJqJqJqJqJqJqJqJqJqJq', false, DATE_SUB(NOW(), INTERVAL 35 DAY), NOW());

-- Insert daily counters for active users
INSERT INTO daily_counters (user_id, start_time, end_time, plan_day, current_day_profit, is_active, is_completed) VALUES
(1, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 15 DAY), INTERVAL 30 DAY), 15, 1.95, true, false),
(2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 10 DAY), INTERVAL 30 DAY), 10, 4.20, true, false),
(4, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 5 DAY), INTERVAL 30 DAY), 5, 11.55, true, false),
(5, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 30 DAY), 3, 24.75, true, false),
(6, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 20 DAY), INTERVAL 30 DAY), 20, 0.35, true, false);

-- Insert deposits with various statuses
INSERT INTO deposits (user_id, plan_id, amount, promo_code_id, bonus_amount, status, created_at, updated_at, approved_at, processed_by, processed_at, notes) VALUES
-- John's deposits
(1, 2, 500.00, 1, 50.00, 'APPROVED', DATE_SUB(NOW(), INTERVAL 30 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 30 DAY), 1, DATE_SUB(NOW(), INTERVAL 30 DAY), 'Initial deposit with WELCOME50 promo'),
(1, NULL, 200.00, NULL, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), NULL, NULL, NULL, 'Additional deposit'),

-- Sarah's deposits
(2, 3, 1000.00, 2, 100.00, 'APPROVED', DATE_SUB(NOW(), INTERVAL 25 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 25 DAY), 1, DATE_SUB(NOW(), INTERVAL 25 DAY), 'Gold plan deposit with BONUS100'),
(2, NULL, 300.00, NULL, 0.00, 'APPROVED', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 10 DAY), 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Additional deposit'),

-- Emma's deposits
(4, 4, 2500.00, 3, 200.00, 'APPROVED', DATE_SUB(NOW(), INTERVAL 20 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 20 DAY), 1, DATE_SUB(NOW(), INTERVAL 20 DAY), 'Platinum plan with VIP200'),
(4, NULL, 500.00, NULL, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), NULL, NULL, NULL, 'Additional deposit'),

-- Alex's deposits
(5, 5, 5000.00, NULL, 0.00, 'APPROVED', DATE_SUB(NOW(), INTERVAL 15 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 15 DAY), 1, DATE_SUB(NOW(), INTERVAL 15 DAY), 'Diamond plan deposit'),

-- Lisa's deposits
(6, 1, 100.00, 1, 50.00, 'APPROVED', DATE_SUB(NOW(), INTERVAL 35 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 35 DAY), 1, DATE_SUB(NOW(), INTERVAL 35 DAY), 'Starter plan with WELCOME50'),

-- Mike's deposits (inactive user)
(3, 2, 500.00, NULL, 0.00, 'REJECTED', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW(), NULL, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Rejected due to insufficient funds');

-- Insert withdrawals with various statuses
INSERT INTO withdrawals (user_id, amount, fee, net_amount, wallet_address, status, created_at, updated_at, processed_at, processed_by, notes) VALUES
-- John's withdrawals
(1, 100.00, 2.00, 98.00, 'TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE', 'APPROVED', DATE_SUB(NOW(), INTERVAL 20 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 20 DAY), 1, 'First withdrawal'),
(1, 200.00, 4.00, 196.00, 'TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE', 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), NULL, NULL, 'Pending withdrawal'),

-- Sarah's withdrawals
(2, 150.00, 3.00, 147.00, 'TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t', 'APPROVED', DATE_SUB(NOW(), INTERVAL 15 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 15 DAY), 1, 'Profit withdrawal'),
(2, 300.00, 6.00, 294.00, 'TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), NULL, NULL, 'Large withdrawal'),

-- Emma's withdrawals
(4, 500.00, 10.00, 490.00, 'TXk8rQqJqJqJqJqJqJqJqJqJqJqJqJqJqJq', 'APPROVED', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 10 DAY), 1, 'Profit withdrawal'),

-- Alex's withdrawals
(5, 1000.00, 20.00, 980.00, 'TYk8rQqJqJqJqJqJqJqJqJqJqJqJqJqJqJq', 'APPROVED', DATE_SUB(NOW(), INTERVAL 8 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 8 DAY), 1, 'Large profit withdrawal'),
(5, 2000.00, 40.00, 1960.00, 'TYk8rQqJqJqJqJqJqJqJqJqJqJqJqJqJqJq', 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), NULL, NULL, 'Major withdrawal'),

-- Lisa's withdrawals
(6, 25.00, 0.50, 24.50, 'TZk8rQqJqJqJqJqJqJqJqJqJqJqJqJqJqJq', 'APPROVED', DATE_SUB(NOW(), INTERVAL 25 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 25 DAY), 1, 'Small withdrawal'),

-- Rejected withdrawal
(1, 500.00, 10.00, 490.00, 'TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE', 'REJECTED', DATE_SUB(NOW(), INTERVAL 12 DAY), NOW(), DATE_SUB(NOW(), INTERVAL 12 DAY), 1, 'Insufficient balance');

-- Insert referral earnings
INSERT INTO referral_earnings (user_id, referrer_id, referred_user_id, deposit_id, amount, commission_type, created_at) VALUES
-- John's referral earnings (direct)
(1, 1, 2, 3, 60.00, 'DIRECT', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, 1, 6, 9, 12.00, 'DIRECT', DATE_SUB(NOW(), INTERVAL 35 DAY)),

-- Sarah's referral earnings (direct)
(2, 2, 4, 5, 42.00, 'DIRECT', DATE_SUB(NOW(), INTERVAL 20 DAY)),

-- Emma's referral earnings (direct)
(4, 4, 5, 7, 90.00, 'DIRECT', DATE_SUB(NOW(), INTERVAL 15 DAY)),

-- Grand referral earnings
(1, 1, 4, 5, 21.00, 'GRAND', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 1, 5, 7, 45.00, 'GRAND', DATE_SUB(NOW(), INTERVAL 15 DAY));

-- Insert transaction logs
INSERT INTO transaction_logs (user_id, transaction_type, transaction_id, amount, balance_before, balance_after, description, ip_address, user_agent, created_at) VALUES
-- John's transactions
(1, 'DEPOSIT', 1, 550.00, 0.00, 550.00, 'Initial deposit with bonus', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'DAILY_PROFIT', NULL, 1.95, 550.00, 551.95, 'Daily profit calculation', '192.168.1.100', 'System', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'WITHDRAWAL', 1, -98.00, 551.95, 453.95, 'Withdrawal processed', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 'REFERRAL_EARNING', 1, 60.00, 453.95, 513.95, 'Referral commission from Sarah', '192.168.1.100', 'System', DATE_SUB(NOW(), INTERVAL 25 DAY)),

-- Sarah's transactions
(2, 'DEPOSIT', 3, 1100.00, 0.00, 1100.00, 'Gold plan deposit with bonus', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(2, 'DAILY_PROFIT', NULL, 4.20, 1100.00, 1104.20, 'Daily profit calculation', '192.168.1.101', 'System', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'WITHDRAWAL', 3, -147.00, 1104.20, 957.20, 'Profit withdrawal', '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)', DATE_SUB(NOW(), INTERVAL 15 DAY)),

-- Emma's transactions
(4, 'DEPOSIT', 5, 2700.00, 0.00, 2700.00, 'Platinum plan with bonus', '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64)', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(4, 'DAILY_PROFIT', NULL, 11.55, 2700.00, 2711.55, 'Daily profit calculation', '192.168.1.102', 'System', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Alex's transactions
(5, 'DEPOSIT', 7, 5000.00, 0.00, 5000.00, 'Diamond plan deposit', '192.168.1.103', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(5, 'DAILY_PROFIT', NULL, 24.75, 5000.00, 5024.75, 'Daily profit calculation', '192.168.1.103', 'System', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Lisa's transactions
(6, 'DEPOSIT', 9, 150.00, 0.00, 150.00, 'Starter plan with bonus', '192.168.1.104', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_7_1)', DATE_SUB(NOW(), INTERVAL 35 DAY)),
(6, 'DAILY_PROFIT', NULL, 0.35, 150.00, 150.35, 'Daily profit calculation', '192.168.1.104', 'System', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Insert audit logs
INSERT INTO audit_logs (user_id, admin_id, action, entity_type, entity_id, old_values, new_values, ip_address, user_agent, created_at) VALUES
(1, 1, 'APPROVE_DEPOSIT', 'DEPOSIT', 1, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2, 1, 'APPROVE_DEPOSIT', 'DEPOSIT', 3, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(4, 1, 'APPROVE_DEPOSIT', 'DEPOSIT', 5, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(5, 1, 'APPROVE_DEPOSIT', 'DEPOSIT', 7, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(6, 1, 'APPROVE_DEPOSIT', 'DEPOSIT', 9, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 35 DAY)),
(1, 1, 'APPROVE_WITHDRAWAL', 'WITHDRAWAL', 1, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 1, 'APPROVE_WITHDRAWAL', 'WITHDRAWAL', 3, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(4, 1, 'APPROVE_WITHDRAWAL', 'WITHDRAWAL', 5, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5, 1, 'APPROVE_WITHDRAWAL', 'WITHDRAWAL', 6, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(6, 1, 'APPROVE_WITHDRAWAL', 'WITHDRAWAL', 7, '{"status": "PENDING"}', '{"status": "APPROVED"}', '192.168.1.1', 'Admin Panel', DATE_SUB(NOW(), INTERVAL 25 DAY));

-- Verify mock data insertion
SELECT 'Mock data insertion complete!' as message;

SELECT 
    'Users created:' as info,
    COUNT(*) as total_users,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_users,
    COUNT(CASE WHEN status = 'INACTIVE' THEN 1 END) as inactive_users
FROM users 
WHERE role = 'USER';

SELECT 
    'Deposits created:' as info,
    COUNT(*) as total_deposits,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved_deposits,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_deposits,
    COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) as rejected_deposits
FROM deposits;

SELECT 
    'Withdrawals created:' as info,
    COUNT(*) as total_withdrawals,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved_withdrawals,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_withdrawals,
    COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) as rejected_withdrawals
FROM withdrawals;

SELECT 
    'Referral earnings created:' as info,
    COUNT(*) as total_earnings,
    SUM(amount) as total_amount
FROM referral_earnings;

SELECT 
    'Transaction logs created:' as info,
    COUNT(*) as total_transactions
FROM transaction_logs; 