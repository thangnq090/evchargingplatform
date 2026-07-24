# Story: Lightweight Payment Orchestrator Workflow

## User Story
As a **System**
I want to **orchestrate payment processing after session completion through a lightweight workflow**
So that **payments are settled reliably without coupling modules**

## Acceptance Criteria
- [ ] Given a completed session, When SessionCompletedEvent fires, Then CalculateCostCommand is triggered
- [ ] Given CostCalculatedEvent, When received, Then ReserveFundsCommand is triggered
- [ ] Given FundsReservedEvent, When received, Then CapturePaymentCommand is triggered
- [ ] Given PaymentCapturedEvent, When received, Then GenerateInvoiceCommand is triggered
- [ ] Given any step failure, When compensation action fires, Then compensating events are published (release quote, void auth, mark failed)
- [ ] Given a failed payment, When retry is triggered, Then exponential backoff is applied
- [ ] Given workflow execution, When tracked, Then state is persisted for observability

## Technical Notes
- Orchestrator is in-process Spring service (not a distributed Saga framework)
- Compensation actions per failure scenario
- Retry policy: 1s, 5s, 15s, 1min, 5min (exponential with jitter)
- Temporal migration path: orchestration logic behind port interface

## Dependencies
- Story 005-001 (PaymentProvider interface)
- Story 003-001 (Session lifecycle)
- Story 004-001 (Cost calculation)
