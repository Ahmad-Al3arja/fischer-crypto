-- Drop all tables to reset the database
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS transaction_logs;
DROP TABLE IF EXISTS referral_earnings;
DROP TABLE IF EXISTS daily_counters;
DROP TABLE IF EXISTS withdrawals;
DROP TABLE IF EXISTS deposits;
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS promo_codes;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS plans;
DROP TABLE IF EXISTS admin_settings;
DROP TABLE IF EXISTS flyway_schema_history;

-- Verify tables are dropped
SELECT 'Database cleaned successfully!' as message; 