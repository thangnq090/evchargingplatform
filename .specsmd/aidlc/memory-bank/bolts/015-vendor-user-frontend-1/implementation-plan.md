---
stage: plan
bolt: 015-vendor-user-frontend-1
created: "2026-07-27T07:26:00Z"
---

# Implementation Plan: Vendor User Frontend (015-vendor-user-frontend-1)

## Objective
Build the Vendor Operator web portal frontend interface in React + TypeScript with Tailwind CSS to enable real-time charger status monitoring via SSE streams, charger availability & maintenance mode toggling (individual and group-scoped), and charging session log filtering with CSV/PDF reporting export options.

## Deliverables
1. **Real-Time Station Monitoring (`/vendor/operations`)**:
   - Visual grid of chargepoints with status badges (`AVAILABLE`, `CHARGING`, `FAULTED`, `UNAVAILABLE`, `MAINTENANCE`).
   - Simulated/live Server-Sent Events (SSE) status stream feed with connection toggle, live indicator, and auto-refreshing telemetry metrics (power draw kW, energy delivered kWh, session elapsed time).
   - Multi-field filtering (Search by Charger ID/Location, Status filter, Connector Type filter, Group Tag filter).

2. **Availability & Maintenance Mode Control**:
   - Quick action to toggle individual charger state between `AVAILABLE` and `MAINTENANCE`.
   - Batch/group-scoped maintenance toggle (e.g. set all chargers in a group to Maintenance mode with maintenance notes).
   - Maintenance modal for documenting reason/notes and estimated downtime.

3. **Charging Session Logs & Export Reporting**:
   - Comprehensive table of historical and active charging sessions for vendor chargepoints.
   - Filtering by preset date ranges (Today, Last 7 Days, Last 30 Days, Custom Range), Session Status, and Charger ID.
   - One-click CSV export and styled PDF summary generation for session metrics.

4. **Navigation & Router Integration**:
   - Add `Vendor Operations` link (`/vendor/operations`) in `Layout.tsx` for `VENDOR_USER` / `VENDOR_ADMIN` roles.
   - Register route `/vendor/operations` in `App.tsx` pointing to `VendorUserOperationsPage`.

## Dependencies
- React, React Router DOM, Lucide Icons, Tailwind CSS.
- Frontend Auth Context (`useAuth`) and API mock state patterns.

## Technical Approach
1. **Feature Directory Structure**: Create `src/features/vendor-user/` containing `types/`, `api/`, `mocks/`, `components/`, `pages/`, and `test/`.
2. **Interactive SSE Mock Service**: Implement a hook `useRealtimeChargerStream` that simulates SSE events pushing real-time status changes and active session power updates when enabled.
3. **Session Export Utilities**: Implement client-side CSV export generator and PDF document formatter utility functions.
4. **Testing**: Write unit/integration tests for the Vendor Operations dashboard in `src/features/vendor-user/test/VendorUserOperationsPage.test.tsx`.

## Acceptance Criteria
- [ ] Real-time station monitoring grid renders charger statuses dynamically and updates live when SSE connection is active.
- [ ] Maintenance mode toggle works for single chargers and batch groups, updating charger availability state instantly.
- [ ] Session reporting table supports date range and status filtering with functional CSV file download and PDF preview/export.
- [ ] Clean TypeScript build with full test coverage and zero compilation or lint errors (`pnpm build` & `pnpm test`).
