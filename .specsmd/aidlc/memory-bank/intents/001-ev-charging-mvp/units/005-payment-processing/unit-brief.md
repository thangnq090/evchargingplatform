---
unit: 005-payment-processing
intent: 001-ev-charging-mvp
phase: inception
status: complete
created: "2026-07-24T15:00:00Z"
updated: "2026-07-25T17:00:00Z"
---

# Unit Brief: Payment Processing

## Purpose
Process payments asynchronously after session completion using a lightweight workflow orchestrator. Payment provider abstraction with MockPayment adapter for MVP. Designed with clear port boundaries so the orchestrator can evolve into a Temporal-managed distributed Saga.

## Scope

### In Scope
- Lightweight asynchronous payment orchestrator (in-process, not full Saga framework)
- 4-step workflow: CalculateCost → ReserveFunds → CapturePayment → GenerateInvoice
- PaymentProvider port interface (authorize, capture, refund, void)
- MockPayment adapter (simulates successful payment)
- Idempotency via session_id + action key
- Exponential backoff retry on payment failure
- Compensation actions per failure scenario
- Session/Payment decoupling (session completes independently)
- Payment state tracking

### Out of Scope
- Distributed Saga framework (Temporal deferred to post-MVP)
- Real payment provider integration (Stripe/Adyen deferred)
- Refund/dispute handling (deferred)
- Subscription or recurring payments

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-14 | Payment Orchestration (workflow, MockPayment, idempotency) | Must |
| FR-15 | Payment/Session Decoupling | Must |

---

## Domain Concepts

### Key Entities
| Entity | Description | Attributes |
|--------|-------------|------------|
| Payment | Payment transaction | id, session_id, amount, currency, status, provider, provider_payment_id, idempotency_key, created_at, updated_at |
| PaymentMethod | Customer payment method | id, customer_id, provider_method_id, type, last_four, expiry, is_default |
| PaymentAttempt | Retry attempt | id, payment_id, attempt_number, status, error_code, attempted_at |

### Key Operations
| Operation | Description | Inputs | Outputs |
|-----------|-------------|--------|---------|
| TriggerPaymentWorkflow | Start payment after session completed | session_id, amount | Workflow started |
| AuthorizePayment | Reserve funds | payment_id, amount, idempotency_key | Authorization |
| CapturePayment | Capture authorized payment | payment_id, amount, idempotency_key | Capture confirmation |
| VoidAuthorization | Release held funds | payment_id | Void confirmation |
| RefundPayment | Process refund | payment_id, amount, reason | Refund confirmation |
| RetryPayment | Retry failed payment | payment_id | PaymentAttempt |
| CompensatePayment | Failure compensation | payment_id, reason | Compensation actions |

### Workflow Steps
```
SessionCompletedEvent
  ↓
Step 1: CalculateCostCommand → CostCalculatedEvent (Billing module)
  ↓
Step 2: ReserveFundsCommand → FundsReservedEvent / ReserveFailedEvent
  ↓ (failure → compensate: release quote, mark billing_failed)
Step 3: CapturePaymentCommand → PaymentCapturedEvent / CaptureFailedEvent
  ↓ (failure → void auth, retry backoff)
Step 4: GenerateInvoiceCommand → InvoiceGeneratedEvent (Billing module)
  ↓ (success → payment settled)
```

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| `003-session-management` | Consumes SessionCompletedEvent |
| `004-billing-pricing` | Cost calculation, invoice generation |

### Depended By
| Unit | Reason |
|------|--------|
| `007-admin-portal` | Aggregates payment data |
| `009-notification` | Payment events for notification |

---

## Technical Context

### Suggested Technology
| Component | Technology |
|-----------|------------|
| Orchestrator | In-process Spring service (no Temporal for MVP) |
| Event Bus | ApplicationEventPublisher (Spring Modulith) |
| Schema | `payment` schema in PostgreSQL |
| API | Internal (domain events + application service) |
| Idempotency | session_id + action composite key |

### Integration Points
| Integration | Type | Protocol |
|-------------|------|----------|
| Session events | Domain events | ApplicationEventPublisher |
| Cost calculation | Internal API | Java interface (billing port) |
| Payment provider | Port/adapter | `PaymentProvider` interface |

---

## Success Criteria

### Functional
- [ ] Session completion triggers payment workflow
- [ ] 4-step workflow executes in order (Calculate → Reserve → Capture → Invoice)
- [ ] MockPayment returns success for all operations
- [ ] Idempotency prevents duplicate processing
- [ ] Payment failure retries with backoff
- [ ] Session completes regardless of payment status (decoupled)

### Non-Functional
- [ ] Idempotency guarantee for all payment operations
- [ ] No cross-module @Transactional spanning modules
- [ ] Each step commits independently

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-005-payment-1 | DDD | S1, S2 | Payment provider port/adapter, MockPayment, idempotency |
| bolt-005-payment-2 | DDD | S3, S4 | Lightweight orchestrator workflow, compensations, retry |
