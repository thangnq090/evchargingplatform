---
unit: 011-admin-portal-frontend
intent: 002-frontend-features
phase: construction
status: completed
created: "2026-07-26T13:47:39Z"
updated: "2026-07-26T13:52:30Z"
---

# Unit Brief: Admin Portal Frontend (011-admin-portal-frontend)

## Purpose
Frontend web application for Platform Administrators to oversee global EV charging operations, perform vendor onboarding, manage global price markups, govern user accounts, and execute full-text global search across the system.

## Scope

### In Scope
- **Vendor Onboarding**: Form and wizard to register new vendors, set contract details, and invite original Vendor Admin users.
- **Global Markup & Financial Overview**: Interface for setting platform markup rates/percentages and visualizing platform-wide revenue aggregated by date range and vendor.
- **User Governance**: Cross-tenant user table to view users, lock/unlock accounts, and trigger credential resets.
- **Global Search UI**: Full-text search bar searching sessions, plates, customer account numbers, and error codes with instant result filtering.

### Out of Scope
- Direct DB operations (consumes REST APIs from Backend Modular Monolith).

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-FE-1 | Vendor Onboarding | Must |
| FR-FE-2 | Global Income & Markup Management | Must |
| FR-FE-3 | User & Credential Governance | Must |
| FR-FE-4 | Cross-Tenant Global Search | Must |

---

## Dependencies
- Backend APIs: `001-identity-service`, `002-station-management`, `004-billing-pricing`, `008-session-search`.

---

## Bolt Plan

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| `013-admin-portal-frontend-1` | Simple / UI | S1, S2, S3, S4 | Complete Admin Portal Frontend UI with Dashboard, Onboarding, Markup & Global Search |
