-- V1_1__Add_foreign_keys.sql
-- Add foreign key constraints after all tables are created

-- Add foreign keys to users table
ALTER TABLE users 
ADD CONSTRAINT fk_users_current_plan FOREIGN KEY (current_plan_id) REFERENCES plans(id),
ADD CONSTRAINT fk_users_referrer FOREIGN KEY (referrer_id) REFERENCES users(id),
ADD CONSTRAINT fk_users_grand_referrer FOREIGN KEY (grand_referrer_id) REFERENCES users(id);

-- Add foreign key to wallets table
ALTER TABLE wallets 
ADD CONSTRAINT fk_wallets_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Add foreign key to deposits table
ALTER TABLE deposits 
ADD CONSTRAINT fk_deposits_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Add foreign key to withdrawals table
ALTER TABLE withdrawals 
ADD CONSTRAINT fk_withdrawals_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Add foreign keys to referral_earnings table
ALTER TABLE referral_earnings 
ADD CONSTRAINT fk_referral_earnings_user FOREIGN KEY (user_id) REFERENCES users(id),
ADD CONSTRAINT fk_referral_earnings_referrer FOREIGN KEY (referrer_id) REFERENCES users(id);

-- Add foreign key to daily_counter table
ALTER TABLE daily_counter 
ADD CONSTRAINT fk_daily_counter_user FOREIGN KEY (user_id) REFERENCES users(id); 