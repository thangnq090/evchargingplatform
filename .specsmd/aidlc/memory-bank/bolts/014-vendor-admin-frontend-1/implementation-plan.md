---
stage: plan
bolt: 014-vendor-admin-frontend-1
created: "2026-07-26T20:22:00Z"
---

# Implementation Plan: Vendor Admin Frontend (014-vendor-admin-frontend-1)

## Objective
Build the Vendor Administrator web portal frontend components in React + TypeScript with Tailwind CSS to enable Chargepoint CRUD operations, group label management, base pricing configuration (in tenths of cents), financial revenue analytics with time-series charts, and vendor staff RBAC onboarding/management.

## Deliverables
- **Vendor Chargepoint Management (`/vendor/chargepoints`)**: Data grid and list of chargepoints, modal to create/edit chargepoint & connectors, group label tag manager, soft-delete confirmation, and base unit pricing per kWh (tenths of cents).
- **Vendor Financial Analytics (`/vendor/analytics`)**: Financial KPI cards (Gross Revenue, Platform Markup, Net Income, Session Count), aggregated charts (Daily, Weekly, Monthly), and chargepoint revenue leaderboard.
- **Vendor Staff Management (`/vendor/staff`)**: Staff user table, invite modal for Vendor Operators/Staff, role permission updater, and staff audit log.
- **Vendor Navigation & Router Integration**: Add Vendor Admin portal navigation links and route definitions in the web application.

## Dependencies
- React + Lucide icons + Recharts / Tailwind CSS UI components.
- Backend services: Identity Service (`001-identity-service`), Station Management (`002-station-management`), Billing & Pricing (`004-billing-pricing`).

## Technical Approach
1. **Feature Directory Structure**: Create `src/features/vendor-admin/` with dedicated components for Chargepoint Management, Revenue Analytics, and Staff Management.
2. **Mock Data & State Services**: Implement mock state stores / hooks in `src/features/vendor-admin/` for local interactivity and API integration readiness.
3. **Route & Layout Integration**: Register `/vendor/chargepoints`, `/vendor/analytics`, and `/vendor/staff` views with layout shell navigation.

## Acceptance Criteria
- [ ] Chargepoints table supports searching, filtering by status/group tag, adding new chargepoints, editing connector details, and setting pricing in tenths of cents.
- [ ] Revenue analytics dashboard features responsive daily/weekly/monthly toggles with bar/line chart visualizers and leaderboard.
- [ ] Staff management view supports inviting new staff members, toggling status/roles, and viewing activity log.
- [ ] Clean typescript build and zero linting/compilation errors.
