---
intent: 002-frontend-features
phase: inception
status: defined
updated: "2026-07-26T13:47:39Z"
---

# Unit Decomposition: Frontend Features (002-frontend-features)

## Overview
Decomposed into 4 target units corresponding to the 4 primary role-based user interfaces.

## Requirement-to-Unit Mapping

| FR | Title | Unit | Priority |
|----|-------|------|----------|
| FR-FE-1 to FR-FE-4 | Admin Onboarding, Global Markup, Income & Global Search | `011-admin-portal-frontend` | Must |
| FR-FE-5 to FR-FE-7 | Vendor Chargepoint Management, Revenue Analytics & Staff | `012-vendor-admin-frontend` | Must |
| FR-FE-8 to FR-FE-10 | Real-time Station Monitoring & Operational Logs | `013-vendor-user-frontend` | Must |
| FR-FE-11 to FR-FE-14 | Customer Web App, Vehicle/RFID & Charging Sessions | `014-customer-portal-frontend` | Must |

## Units Summary

| # | Unit | Role | Backend Services Consumed | Bolt Type | Priority |
|---|------|------|---------------------------|-----------|----------|
| 11 | `011-admin-portal-frontend` | ADMIN | Identity, Station, Billing, Session Search | Simple / UI | Must |
| 12 | `012-vendor-admin-frontend` | VENDOR ADMIN | Identity, Station, Billing | Simple / UI | Must |
| 13 | `013-vendor-user-frontend` | VENDOR USER | Station, Session Management | Simple / UI | Must |
| 14 | `014-customer-portal-frontend` | CUSTOMER | Identity, Vehicle, Session, Payment | Simple / UI | Must |
