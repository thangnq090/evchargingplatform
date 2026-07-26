# DDD Stage 1: Domain Model — 007-payment-processing-1

## 1. Domain Entities & Value Objects

### Entities
1. **`Payment`** (Aggregate Root)
   - **ID**: `PaymentId` (UUID)
   - **Attributes**:
     - `sessionId`: `SessionId` (UUID) - reference to Session
     - `customerId`: `CustomerId` (UUID) - reference to Customer/User
     - `vehicleId`: `VehicleId` (UUID) - reference to Vehicle
     - `chargePointId`: `ChargePointId` (UUID) - reference to ChargePoint
     - `amount`: `Money` (BigDecimal amount, Currency currency)
     - `status`: `PaymentStatus` (`PENDING`, `AUTHORIZED`, `CAPTURED`, `FAILED`, `VOIDED`, `REFUNDED`)
     - `paymentMethodId`: `PaymentMethodId` (UUID)
     - `providerPaymentId`: `String` (external reference ID)
     - `idempotencyKey`: `String` (`session_id + action`)
     - `createdAt`: `Instant`
     - `updatedAt`: `Instant`
   - **Behavior**:
     - `markAuthorized(providerPaymentId)`
     - `markCaptured()`
     - `markFailed(reason)`
     - `markVoided()`

2. **`PaymentAttempt`** (Entity inside Payment aggregate or child tracking entity)
   - **ID**: `PaymentAttemptId` (UUID)
   - **Attributes**:
     - `paymentId`: `PaymentId`
     - `attemptNumber`: `int`
     - `status`: `PaymentAttemptStatus` (`SUCCESS`, `FAILED`)
     - `errorCode`: `String`
     - `errorMessage`: `String`
     - `attemptedAt`: `Instant`

3. **`PaymentMethod`** (Entity / Value Object)
   - **ID**: `PaymentMethodId` (UUID)
   - **Attributes**: `customerId`, `providerMethodId`, `type` (`CREDIT_CARD`), `lastFour`, `expiry`, `isDefault`

### Value Objects
1. **`Money`**: `amount` (BigDecimal), `currency` (Currency)
2. **`PaymentId`**, **`PaymentMethodId`**, **`SessionId`**, **`CustomerId`**, **`VehicleId`**, **`ChargePointId`**
3. **`IdempotencyKey`**: `String` formatted as `session:{sessionId}:{action}`

---

## 2. Aggregates & Boundaries

```
┌────────────────────────────────────────────────────────┐
│ Payment Aggregate                                      │
│                                                        │
│  ┌─────────────────────────┐                           │
│  │ Payment (Root)          │                           │
│  └───────────┬─────────────┘                           │
│              │ 1                                       │
│              │ *                                       │
│  ┌───────────▼─────────────┐                           │
│  │ PaymentAttempt          │                           │
│  └─────────────────────────┘                           │
└────────────────────────────────────────────────────────┘
```

---

## 3. Domain Events

1. **`PaymentAuthorizedEvent`**: `paymentId`, `sessionId`, `amount`, `providerPaymentId`, `occurredAt`
2. **`PaymentCapturedEvent`**: `paymentId`, `sessionId`, `amount`, `providerPaymentId`, `occurredAt`
3. **`PaymentFailedEvent`**: `paymentId`, `sessionId`, `reason`, `errorCode`, `occurredAt`
4. **`PaymentVoidedEvent`**: `paymentId`, `sessionId`, `reason`, `occurredAt`

---

## 4. Domain & Port Interfaces (Services)

1. **`PaymentProvider`** (Port interface for external or mock payment gateways):
   - `PaymentAuthorizationResult authorize(PaymentMethodId methodId, Money amount, IdempotencyKey key)`
   - `PaymentCaptureResult capture(String providerPaymentId, Money amount, IdempotencyKey key)`
   - `PaymentVoidResult voidAuthorization(String providerPaymentId)`
   - `PaymentRefundResult refund(String providerPaymentId, Money amount, String reason)`

2. **`MockPaymentAdapter`** (Adapter implementing `PaymentProvider` for MVP):
   - Simulates successful authorizations & captures.

3. **`PaymentRepository`** (Port interface):
   - `Payment save(Payment payment)`
   - `Optional<Payment> findById(PaymentId id)`
   - `Optional<Payment> findBySessionId(SessionId sessionId)`
   - `Optional<Payment> findByIdempotencyKey(String idempotencyKey)`

---

## 5. Ubiquitous Language Glossary

- **Payment Orchestrator**: The process coordinating the step-by-step charging payment flow (Calculate Cost -> Reserve Funds -> Capture Payment -> Generate Invoice).
- **Idempotency Key**: Unique string generated per step/action to ensure safe retries without duplicate charges.
- **Compensation Action**: A fallback operation (e.g. void authorization) executed when a subsequent step in the workflow fails.
