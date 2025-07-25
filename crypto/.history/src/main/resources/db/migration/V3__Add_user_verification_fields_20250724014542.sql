-- V3__Add_user_verification_fields.sql
-- Add user verification and additional fields

-- Add verification fields to users table
ALTER TABLE users 
ADD COLUMN email VARCHAR(255) UNIQUE,
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN phone_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN verification_token VARCHAR(255),
ADD COLUMN verification_token_expires DATETIME,
ADD COLUMN last_login DATETIME,
ADD COLUMN login_attempts INT DEFAULT 0,
ADD COLUMN locked_until DATETIME;

-- Add indexes for new fields
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_verification_token ON users(verification_token);
CREATE INDEX idx_users_last_login ON users(last_login); 