-- V8__add_vendor_markup.sql
-- Adds markup_basis_points column to vendors table
-- This column stores the platform's margin on the vendor's charging prices
-- Stored as basis points (1 BP = 0.01%, e.g., 1500 BP = 15.00%)

ALTER TABLE identity.vendors
ADD COLUMN markup_basis_points INTEGER NOT NULL DEFAULT 0
    CHECK (markup_basis_points BETWEEN 0 AND 10000);

COMMENT ON COLUMN identity.vendors.markup_basis_points IS 'Platform markup in basis points (1 BP = 0.01%)';