-- V301__create_billing_schema.sql
-- Creates the billing schema and tables for invoice management and billing accounts

CREATE SCHEMA IF NOT EXISTS billing;

COMMENT ON SCHEMA billing IS 'Billing and Invoicing: session invoices, line items, and billing accounts';

CREATE TABLE billing.invoices (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    vendor_id UUID NOT NULL,
    total_amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PAID', 'VOIDED')),
    created_at TIMESTAMPTZ NOT NULL,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE billing.invoice_line_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES billing.invoices(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    unit_price_amount NUMERIC(19,4) NOT NULL,
    unit_price_currency VARCHAR(3) NOT NULL,
    quantity NUMERIC(19,4) NOT NULL,
    total_amount NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL
);

CREATE TABLE billing.billing_accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL UNIQUE,
    balance_amount NUMERIC(19,4) NOT NULL,
    balance_currency VARCHAR(3) NOT NULL,
    total_spent_amount NUMERIC(19,4) NOT NULL,
    total_spent_currency VARCHAR(3) NOT NULL,
    last_billed_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0
);

-- Performance and query indexes
CREATE INDEX idx_invoices_vendor_created ON billing.invoices(vendor_id, created_at DESC);
CREATE INDEX idx_invoices_created ON billing.invoices(created_at DESC);
CREATE INDEX idx_line_items_invoice ON billing.invoice_line_items(invoice_id);

COMMENT ON TABLE billing.invoices IS 'Generated invoices for EV charging sessions';
COMMENT ON TABLE billing.invoice_line_items IS 'Detailed fee line items associated with each invoice';
COMMENT ON TABLE billing.billing_accounts IS 'Customer billing profiles tracking balance and total platform spending';
