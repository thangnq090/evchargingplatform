---
unit: 003-session-management
intent: 001-ev-charging-mvp
phase: construction
status: complete
implemented: true
created: '2026-07-24T15:00:00Z'
updated: '2026-07-24T15:00:00Z'
---

# Story: Charging Session Lifecycle

## User Story
As a **Customer**
I want to **start and stop a charging session at a chargepoint**
So that **my vehicle gets charged and I pay only for what I use**

## Acceptance Criteria
- [ ] Given a customer at an available chargepoint with a vehicle, When they start a session, Then a CHARGING session is created
- [ ] Given an active session, When meter readings are received, Then they are recorded with timestamp and energy value
- [ ] Given a CHARGING session, When stopped, Then session status becomes COMPLETED, total energy and amount are calculated
- [ ] Given a session with an error, When stopped, Then error code is recorded, session status is FAILED
- [ ] Given session start, When chargepoint is UNAVAILABLE, Then 422 is returned

## Technical Notes
- Session statuses: PENDING → CHARGING → COMPLETED / FAILED
- Marked-up unit rate captured at start time
- Session belongs to the month it starts

## Dependencies
- Story 002-001 (Chargepoint CRUD)
- Story 001-003 (Customer registration)
