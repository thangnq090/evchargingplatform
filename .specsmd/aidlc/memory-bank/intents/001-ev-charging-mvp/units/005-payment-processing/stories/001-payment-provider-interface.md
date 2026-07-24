# Story: PaymentProvider Interface and MockPayment Adapter

## User Story
As a **Developer**
I want to **define a PaymentProvider interface and implement a MockPayment adapter**
So that **payment orchestration can be developed and tested independently of external providers**

## Acceptance Criteria
- [ ] Given payment processing, When authorize is called, Then PaymentProvider interface handles it
- [ ] Given MockPayment adapter, When authorize/capture/refund/void are called, Then success is simulated
- [ ] Given provider abstraction, When a new provider is added, Then only an adapter implementation is needed
- [ ] Given any payment operation, When called with idempotency_key, Then same key returns same result

## Technical Notes
- Interface: `PaymentProvider` with authorize, capture, refund, void
- MockPayment returns configurable success/failure for testing
- Idempotency key: session_id + action composite

## Dependencies
- None (can be built in parallel)
