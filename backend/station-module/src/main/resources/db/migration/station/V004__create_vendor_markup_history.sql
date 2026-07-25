-- V004__create_vendor_markup_history.sql
-- Creates the vendor markup audit history table

CREATE TABLE station.vendor_markup_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id UUID NOT NULL REFERENCES identity.vendors(id),
    markup_basis_points INTEGER NOT NULL CHECK (markup_basis_points BETWEEN 0 AND 10000),
    changed_by UUID NOT NULL REFERENCES identity.users(id),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_markup_history_vendor_id ON station.vendor_markup_history (vendor_id);
CREATE INDEX idx_markup_history_changed_at ON station.vendor_markup_history (changed_at);

COMMENT ON TABLE station.vendor_markup_history IS 'Audit log of vendor markup changes';
COMMENT ON COLUMN station.vendor_markup_history.markup_basis_points IS 'Markup in basis points (1 BP = 0.01%)';