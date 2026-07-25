-- V1__init_identity_schema.sql
-- Identity & Access Service - Initial Schema
-- Creates vendors, users, and invitations tables in the identity schema.

CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE identity.vendors (
  id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  name       VARCHAR(100) NOT NULL UNIQUE,
  status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE identity.users (
  id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(20)  NOT NULL,
  vendor_id     UUID         REFERENCES identity.vendors(id),
  status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE identity.invitations (
  id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  email      VARCHAR(255) NOT NULL,
  vendor_id  UUID         NOT NULL REFERENCES identity.vendors(id),
  role       VARCHAR(20)  NOT NULL,
  token      VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ  NOT NULL,
  status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Indexes for common lookup patterns
CREATE INDEX idx_identity_users_email     ON identity.users(email);
CREATE INDEX idx_identity_users_vendor_id ON identity.users(vendor_id);
CREATE INDEX idx_identity_vendors_name    ON identity.vendors(name);
CREATE INDEX idx_identity_invitations_token ON identity.invitations(token);
CREATE INDEX idx_identity_invitations_vendor_id ON identity.invitations(vendor_id);
