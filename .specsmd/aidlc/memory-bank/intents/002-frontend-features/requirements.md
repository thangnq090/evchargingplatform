# Functional & Non-Functional Requirements: Frontend Features

## Functional Requirements (FR)

### Admin Portal (ADMIN)
- **FR-FE-1 (Vendor Onboarding)**: Platform Admin can view all registered vendors, onboard/invite new vendors, and create initial Vendor Admin user accounts.
- **FR-FE-2 (Global Income & Markup Management)**: Platform Admin can configure global markup percentages/fixed rates added to vendor base prices, and view platform-wide income aggregated over custom date ranges.
- **FR-FE-3 (User & Credential Governance)**: Platform Admin can view all platform users across roles, lock/unlock accounts, and initiate credential resets.
- **FR-FE-4 (Cross-Tenant Global Search)**: Platform Admin can perform full-text search across all sessions, plates, customer account numbers, and error codes.

### Vendor Admin Portal (VENDOR ADMIN)
- **FR-FE-5 (Chargepoint Management & Grouping)**: Vendor Admin can register new chargepoints, set unique IDs, organize chargers into group labels, and set base unit prices (in tenths of cents).
- **FR-FE-6 (Vendor Financial Dashboard)**: Vendor Admin can view total vendor income, monthly trends, and revenue breakdowns per chargepoint and connector over daily/weekly/monthly views.
- **FR-FE-7 (Vendor Staff Management)**: Vendor Admin can invite and manage Vendor Users, assigning granular permissions for station management and reporting.

### Vendor Operator View (VENDOR USER)
- **FR-FE-8 (Real-time Station Monitoring)**: Vendor Users can monitor real-time charger status (Available, Charging, Faulted, Unavailable) via live SSE updates.
- **FR-FE-9 (Station Maintenance & Group Control)**: Vendor Users can toggle charger availability state and view operational logs for chargepoints within their assigned group labels.
- **FR-FE-10 (Charging Session Reporting)**: Vendor Users can generate and export session summary reports for specific chargepoints on selected dates.

### Customer Portal / Web App (CUSTOMER)
- **FR-FE-11 (Customer Registration & Profile)**: Customers can register, edit contact details, view unique account numbers, and manage preferences.
- **FR-FE-12 (Vehicle & RFID Management)**: Customers can register vehicles with license plates, associate RFID tags/cards, and de-list old vehicles.
- **FR-FE-13 (Active Session & Remote Start/Stop)**: Customers can view real-time charging session metrics (kWh delivered, duration, calculated cost based on marked-up rate).
- **FR-FE-14 (Session History & Invoices)**: Customers can view historic charging sessions with itemized breakdowns and monthly total statements.

---

## Non-Functional Requirements (NFR)

- **NFR-FE-1 (Responsiveness & Design)**: Interfaces must be responsive across mobile, tablet, and desktop viewports, using a modern dark/light design system with clear visual hierarchy.
- **NFR-FE-2 (Real-time Updates)**: Charger availability and live charging session metrics must update within < 2 seconds of backend event dispatch via Server-Sent Events (SSE) or WebSockets.
- **NFR-FE-3 (Accessibility & Performance)**: Fast initial load times (< 1.5s LCP), WCAG 2.1 AA accessibility standards for contrast and keyboard navigation.
- **NFR-FE-4 (Security)**: Secure JWT token storage in HTTP-only cookies / secure local storage with automatic token refresh and role-based route guards.
