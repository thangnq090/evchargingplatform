# Intent: Frontend Web Applications (002-frontend-features)

## Overview
Develop modern, responsive web application interfaces tailored for four distinct user roles in the EV Charging Platform:
1. **Platform Administrator (ADMIN)**: Global platform governance, vendor onboarding, global revenue & markup management, user credential resets.
2. **Vendor Admin (VENDOR ADMIN)**: Chargepoint registry management, station availability/grouping control, organization-wide financial analytics & revenue breakdowns.
3. **Vendor User (VENDOR USER)**: Day-to-day station monitoring, group-scoped chargepoint operations, local operational logging/reporting.
4. **Customer (CUSTOMER)**: Customer portal/app for managing vehicles (RFID/plate), viewing live/historical charging sessions, payment processing summaries, and billing histories.

## Intent ID
`002-frontend-features`

## Status
**In Progress** - Inception phase started

## Primary Actors & Portals

| Actor | Portal / Frontend Scope | Target Features |
|-------|------------------------|-----------------|
| **ADMIN** | Platform Admin Portal | Vendor onboarding, global markup, system-wide financial reporting, cross-tenant full-text search, user management. |
| **VENDOR ADMIN** | Vendor Portal (Admin View) | Station & connector management, group labeling, tariff configuration, vendor revenue analytics, vendor user RBAC management. |
| **VENDOR USER** | Vendor Portal (Operator View) | Real-time station status monitoring, operational reporting, maintenance status updates (per assigned permissions). |
| **CUSTOMER** | Customer Web App / Portal | Vehicle registry (plates, RFID association), session history & receipts, active charging session view, personal payment methods. |

## Target Technology Stack (Frontend)
- **Framework**: Vite + React / TypeScript (or Next.js depending on portal requirements)
- **Styling**: Modern CSS / CSS Modules with rich design system tokens (dark mode support, glassmorphism, responsive grid layout)
- **State Management & API**: React Query / Axios consuming Modular Monolith REST APIs & SSE streams for live charger updates
- **Design Principles**: Premium visual aesthetics, dynamic micro-interactions, responsive mobile/desktop UI.

## Modular Monolith Backend Dependencies
- `001-identity-service`: JWT auth, RBAC role scopes, user session control.
- `002-station-management`: Chargepoint CRUD, status SSE stream, markup/rates display.
- `003-session-management`: Active & historic charging sessions.
- `004-billing-pricing`: Income analytics, tariff/markup breakdown.
- `005-payment-processing`: Customer payment receipts & provider state.
- `006-vehicle-management`: Vehicle registration & RFID management.
- `008-session-search`: Admin FTS search bar.

## Acceptance Criteria for Inception Complete
- [x] Intent registered (`002-frontend-features/intent.md`)
- [ ] Requirements documented (`requirements.md` covering ADMIN, VENDOR ADMIN, VENDOR USER, CUSTOMER)
- [ ] System context & frontend architecture mapped (`system-context.md`)
- [ ] Units decomposed by role/portal (`units.md`)
- [ ] User stories created with UI acceptance criteria
- [ ] Execution bolts planned for construction
