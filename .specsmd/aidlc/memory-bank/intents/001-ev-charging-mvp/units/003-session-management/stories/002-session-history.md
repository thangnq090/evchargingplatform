---
unit: 003-session-management
intent: 001-ev-charging-mvp
phase: construction
status: complete
implemented: true
created: '2026-07-24T15:00:00Z'
updated: '2026-07-24T15:00:00Z'
---

# Story: Customer Session History and Monthly Totals

## User Story
As a **Customer**
I want to **view my charging session history with monthly totals**
So that **I can track my charging costs and energy usage**

## Acceptance Criteria
- [ ] Given a customer with sessions, When they view history, Then sessions are grouped by month
- [ ] Given a monthly view, When displayed, Then total sessions, total energy (kWh), and total amount are shown
- [ ] Given the current month in progress, When viewed, Then partial month totals are included

## Dependencies
- Story 003-001 (Session lifecycle)
