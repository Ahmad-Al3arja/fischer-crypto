-- V2__Add_wallet_change_requests.sql
-- Add wallet change requests functionality

-- Add address_set column to wallets table
ALTER TABLE wallets ADD COLUMN address_set BOOLEAN NOT NULL DEFAULT FALSE;

-- Create wallet_change_requests table
CREATE TABLE wallet_change_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_address VARCHAR(255) NOT NULL,
    new_address VARCHAR(255) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by BIGINT,
    admin_notes TEXT,
    created_at DATETIME NOT NULL,
    processed_at DATETIME,
    
    -- Add foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (processed_by) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX idx_wallet_change_requests_user_id ON wallet_change_requests(user_id);
CREATE INDEX idx_wallet_change_requests_status ON wallet_change_requests(status);
CREATE INDEX idx_wallet_change_requests_created_at ON wallet_change_requests(created_at);

-- Update existing wallets to mark address_set as true if they have an address
UPDATE wallets SET address_set = TRUE WHERE usdt_address IS NOT NULL AND usdt_address != '';

-- Verify the setup
SELECT 'Wallet change requests setup complete!' as message;
SELECT 
    'Wallets updated:' as info,
    COUNT(*) as total_wallets,
    SUM(CASE WHEN address_set = TRUE THEN 1 ELSE 0 END) as addresses_set
FROM wallets; 