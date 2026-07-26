---
unit: 012-vendor-admin-frontend
intent: 002-frontend-features
phase: inception
status: defined
created: "2026-07-26T13:47:39Z"
updated: "2026-07-26T13:52:30Z"
---

# Unit Brief: Vendor Admin Frontend (012-vendor-admin-frontend)

## Purpose
Frontend portal for Vendor Administrators to manage their owned chargepoint infrastructure, configure group labels, analyze vendor revenue, set base unit pricing, and manage vendor staff (Vendor Users).

## Scope

### In Scope
- **Chargepoint Management**: Interactive table & grid to add, update, soft-delete chargepoints, configure connectors, and assign group labels.
- **Base Pricing**: Configure unit prices in tenths of cents per chargepoint/group.
- **Vendor Analytics Dashboard**: Income charts, session breakdown charts by chargepoint over daily, weekly, and monthly periods.
- **Staff Management**: Invite vendor operators, assign permissions, and view staff activity.

### Out of Scope
- Direct device sockets (uses backend station management APIs).

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-FE-5 | Chargepoint Management & Grouping | Must |
| FR-FE-6 | Vendor Financial Dashboard | Must |
| FR-FE-7 | Vendor Staff Management | Must |

---

## Dependencies
- Backend APIs: `001-identity-service`, `002-station-management`, `004-billing-pricing`.

---

## Bolt Plan

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| `014-vendor-admin-frontend-1` | Simple / UI | S1, S2, S3 | Complete Vendor Admin Portal UI with Chargepoint Management, Revenue Analytics & Staff RBAC |
