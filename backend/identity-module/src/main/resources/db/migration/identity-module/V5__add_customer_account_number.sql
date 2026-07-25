ALTER TABLE identity.users ADD COLUMN IF NOT EXISTS phone VARCHAR(32);
ALTER TABLE identity.users ADD COLUMN IF NOT EXISTS account_number VARCHAR(64);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_account_number ON identity.users(account_number) WHERE account_number IS NOT NULL;
