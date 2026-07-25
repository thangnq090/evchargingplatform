-- V7__add_rbac_credential_management.sql
-- Alter users to add must_change_password
ALTER TABLE identity.users ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS identity.refresh_tokens (
  id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id              UUID         NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
  token_hash           VARCHAR(64)  NOT NULL UNIQUE,
  issued_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  expires_at           TIMESTAMPTZ  NOT NULL,
  revoked_at           TIMESTAMPTZ,
  replaced_by_token_id UUID         REFERENCES identity.refresh_tokens(id),
  user_agent           VARCHAR(512),
  ip_address           VARCHAR(45)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON identity.refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_hash ON identity.refresh_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_active 
  ON identity.refresh_tokens(user_id, expires_at) 
  WHERE revoked_at IS NULL;
