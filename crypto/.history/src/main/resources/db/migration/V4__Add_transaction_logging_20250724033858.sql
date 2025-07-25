-- V4__Add_transaction_logging.sql
-- Fix database schema to match entity changes

-- Update users table
ALTER TABLE users CHANGE COLUMN username display_username VARCHAR(50) NOT NULL;
ALTER TABLE users DROP INDEX username;
CREATE UNIQUE INDEX idx_users_display_username ON users(display_username);

-- Update withdrawals table  
ALTER TABLE withdrawals DROP COLUMN wallet_address;
-- Keep only usdt_address, rename to wallet_address
ALTER TABLE withdrawals CHANGE COLUMN usdt_address wallet_address VARCHAR(255) NOT NULL;

-- Add plan_id to deposits table if missing
ALTER TABLE deposits ADD COLUMN plan_id BIGINT;
ALTER TABLE deposits ADD CONSTRAINT fk_deposits_plan FOREIGN KEY (plan_id) REFERENCES plans(id);

-- Add promo_code_id to deposits table if missing  
ALTER TABLE deposits ADD COLUMN promo_code_id BIGINT;
ALTER TABLE deposits ADD CONSTRAINT fk_deposits_promo_code FOREIGN KEY (promo_code_id) REFERENCES promo_codes(id);

-- Update promo_codes table column name
ALTER TABLE promo_codes CHANGE COLUMN bonus_amount bonus_value DECIMAL(19,2) NOT NULL;
ALTER TABLE promo_codes CHANGE COLUMN max_uses usage_limit INT NOT NULL;
ALTER TABLE promo_codes CHANGE COLUMN current_uses used_count INT NOT NULL DEFAULT 0;

-- Fix admin user with correct username field
UPDATE users SET display_username = 'admin' WHERE phone_number = '1234567890'; 