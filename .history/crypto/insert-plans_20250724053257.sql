-- Insert investment plans
INSERT INTO plans (name, price, monthly_profit, daily_profit_min, daily_profit_max, plan_level) VALUES
('Starter', 100.00, 10.00, 0.30, 0.40, 1),
('Silver', 500.00, 60.00, 1.80, 2.20, 2),
('Gold', 1000.00, 130.00, 4.00, 4.80, 3),
('Platinum', 2500.00, 350.00, 10.50, 12.60, 4),
('Diamond', 5000.00, 750.00, 22.50, 27.00, 5)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
price = VALUES(price),
monthly_profit = VALUES(monthly_profit),
daily_profit_min = VALUES(daily_profit_min),
daily_profit_max = VALUES(daily_profit_max),
plan_level = VALUES(plan_level);

-- Verify plans were inserted
SELECT 'Plans inserted successfully!' as message;
SELECT * FROM plans ORDER BY plan_level; 