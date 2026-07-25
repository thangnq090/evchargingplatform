-- V2__seed_superadmin.sql
-- Seed initial platform superadmin user account
-- Default Credentials:
--   Email: superadmin@evcharging.test
--   Password: SuperAdmin@Pass1!

INSERT INTO identity.users (id, name, email, password_hash, role, vendor_id, status, created_at, updated_at)
VALUES (
  '00000000-0000-0000-0000-000000000001',
  'Platform SuperAdmin',
  'superadmin@evcharging.test',
  '$2a$12$N9qo8uLOickgx2ZMRZoMyePmuP4/sr6XBiWOxoksEYBLBz9L6lCaq',
  'ADMIN',
  NULL,
  'ACTIVE',
  NOW(),
  NOW()
)
ON CONFLICT (email) DO NOTHING;
