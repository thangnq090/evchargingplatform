---
stage: plan
bolt: 009-admin-portal-1
unit: 007-admin-portal
created: 2026-07-26T13:36:00Z
---

# Implementation Plan: 009-admin-portal-1

## Objective
Implement aggregated REST API endpoints for Admin and Vendor Dashboards without introducing business logic or duplicate persistent state in this layer.

## Deliverables
- `AdminPortalController.java` REST controller with endpoints for Admin & Vendor portal dashboards.
- `AdminDashboardSummaryResponse.java` and `VendorDashboardSummaryResponse.java` DTOs.
- `AdminDashboardApplicationService.java` aggregating data across Station, Session, Billing, and Identity application services/repositories.

## Dependencies
- Uses module application service/repository interfaces or published events/queries from:
  - Identity (`com.evcharging.identity`)
  - Station (`com.evcharging.station`)
  - Session (`com.evcharging.session`)
  - Billing (`com.evcharging.billing`)

## Technical Approach
- Place API layer in `backend/admin-portal-module/src/main/java/com/evcharging/adminportal/`
- Read-only data aggregation via injected application services/ports.
- Enforce role-based access:
  - `/api/v1/admin/dashboard` requires `ADMIN` role.
  - `/api/v1/vendor/dashboard` requires `VENDOR_ADMIN` or `VENDOR_USER` role, scoped by `vendor_id` from JWT context.

## Acceptance Criteria
- [ ] Admin dashboard endpoint returns aggregated platform metrics (income, active sessions, vendor counts).
- [ ] Vendor dashboard endpoint returns vendor-scoped metrics filtered by JWT `vendor_id`.
- [ ] Role-based authorization enforced via `@PreAuthorize`.
