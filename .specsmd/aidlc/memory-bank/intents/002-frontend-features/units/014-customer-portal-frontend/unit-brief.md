---
unit: 014-customer-portal-frontend
intent: 002-frontend-features
phase: inception
status: defined
created: "2026-07-26T13:47:39Z"
updated: "2026-07-26T13:52:30Z"
---

# Unit Brief: Customer Portal Frontend (014-customer-portal-frontend)

## Purpose
Mobile-friendly customer web application enabling EV drivers to manage their registered vehicles (license plates and RFID numbers), monitor live charging sessions, view session history, and inspect receipts/invoices.

## Scope

### In Scope
- **Customer Registration & Profile**: Self-service registration, profile details, and account number display.
- **Vehicle & RFID Registry**: Add/edit/de-list vehicles, assign/re-assign license plates and RFID card numbers.
- **Active Charging Monitor**: Real-time view of active session (energy kWh delivered, live duration, calculated cost).
- **Session History & Monthly Summaries**: Searchable charging history with itemized cost breakdowns and downloadable statements.

### Out of Scope
- Direct payment provider gateway SDK integration beyond tokenized summary view.

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-FE-11 | Customer Registration & Profile | Must |
| FR-FE-12 | Vehicle & RFID Management | Must |
| FR-FE-13 | Active Session & Remote Start/Stop | Must |
| FR-FE-14 | Session History & Invoices | Must |

---

## Dependencies
- Backend APIs: `001-identity-service`, `003-session-management`, `005-payment-processing`, `006-vehicle-management`.

---

## Bolt Plan

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| `016-customer-portal-frontend-1` | Simple / UI | S1, S2, S3, S4 | Complete Customer Web Portal UI with Vehicle/RFID management & Live Session Monitor |
