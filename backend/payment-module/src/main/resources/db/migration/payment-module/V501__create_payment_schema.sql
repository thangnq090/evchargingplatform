-- Migration for Payment Module Schema
CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE payment.payments (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    vehicle_id UUID,
    charge_point_id UUID,
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(30) NOT NULL,
    payment_method_id UUID,
    provider_payment_id VARCHAR(100),
    idempotency_key VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE payment.payment_attempts (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payment.payments(id),
    attempt_number INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    error_code VARCHAR(50),
    error_message TEXT,
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_payments_session_id ON payment.payments(session_id);
CREATE INDEX idx_payments_idempotency_key ON payment.payments(idempotency_key);
