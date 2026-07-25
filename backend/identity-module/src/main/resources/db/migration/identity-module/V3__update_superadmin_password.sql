-- V3__update_superadmin_password.sql
-- Update superadmin password hash to the exact BCrypt hash for "SuperAdmin@Pass1!"

UPDATE identity.users
SET password_hash = '$2a$12$N9qo8uLOickgx2ZMRZoMyePmuP4/sr6XBiWOxoksEYBLBz9L6lCaq'
WHERE email = 'superadmin@evcharging.test';
