# DDD Stage 2: Technical Design — 007-payment-processing-1

## 1. Technical Architecture & Package Structure

The `payment-module` will be created under `backend/payment-module` matching existing Spring Modulith / Maven multi-module structure (`backend/billing-module`, `backend/session-module`, etc.).

```
backend/payment-module/src/main/java/com/evcharging/payment/
├── api/
│   ├── controller/          # REST endpoints (if required for manual trigger/status query)
│   └── dto/                 # Request/Response DTOs
├── application/
│   ├── service/             # PaymentApplicationService, PaymentOrchestrator
│   └── listener/            # SessionCompletedEventListener
├── domain/
│   ├── model/               # Payment, PaymentAttempt, PaymentStatus, Money, etc.
│   ├── repository/          # PaymentRepository interface
│   └── port/                # PaymentProvider interface
└── infrastructure/
    ├── adapter/             # MockPaymentAdapter (implements PaymentProvider)
    └── persistence/         # JPA Entity, Repository implementation, Mapper
```

---

## 2. Payment Orchestration Workflow Sequence

```
SessionCompletedEvent (Session Module)
        │
        ▼
SessionCompletedEventListener (Payment Module)
        │
        ├─► Step 1: Calculate Cost via Billing API/Port
        │      └─► Cost Calculated (Money amount)
        │
        ├─► Step 2: Reserve/Authorize Funds (`PaymentProvider.authorize`)
        │      ├─► Success: Payment status -> AUTHORIZED
        │      └─► Failure: Mark payment FAILED -> End workflow
        │
        ├─► Step 3: Capture Payment (`PaymentProvider.capture`)
        │      ├─► Success: Payment status -> CAPTURED
        │      └─► Failure: Trigger Compensation (`PaymentProvider.voidAuthorization`)
        │                    -> Mark payment FAILED
        │
        └─► Step 4: Trigger Invoice Generation via Billing API/Port
               └─► Success: Workflow Completed
```

---

## 3. Database Schema (`payment` schema)

```sql
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
```

---

## 4. Idempotency & Retry Strategy

- **Idempotency Key**: Generated as `session:{sessionId}:{action}`.
- **Spring Application Event listener**: `@EventListener` / `@TransactionalEventListener` ensures idempotent handling using unique constraint check on `idempotency_key` or `session_id`.
- **Retry Mechanism**: In-memory retry with exponential backoff on retryable provider exceptions before triggering compensation.
