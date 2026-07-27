-- V105__create_vendor_markups.sql
-- Creates the vendor markups table for per-vendor pricing rules

CREATE TABLE station.vendor_markups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id UUID NOT NULL REFERENCES identity.vendors(id),
    markup_basis_points INTEGER NOT NULL CHECK (markup_basis_points BETWEEN 0 AND 10000),
    effective_from TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vendor_markups_vendor_id ON station.vendor_markups (vendor_id);

COMMENT ON TABLE station.vendor_markups IS 'Per-vendor pricing markup rules (basis points)';
