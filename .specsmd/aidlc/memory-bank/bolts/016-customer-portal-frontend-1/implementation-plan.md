# Implementation Plan - Customer Portal Frontend (016-customer-portal-frontend-1)

Construct the mobile-first/desktop customer portal UI enabling EV drivers to register/login, manage profiles & account IDs, register vehicles with license plates and RFID tags, monitor live active charging sessions with real-time kWh and cost updates, and inspect session history & itemized billing invoices.

## User Review Required

> [!IMPORTANT]
> The Customer Portal provides dedicated self-service navigation for EV Drivers (`ROLE_CUSTOMER`). It includes registration/auth, vehicle & RFID card registry management, active session monitoring, and searchable session history with itemized invoice modals.

## Proposed Changes

### Shared & Auth Enhancements

#### [MODIFY] [authApi.ts](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/features/auth/api/authApi.ts)
- Add `registerCustomer` method interfacing with `POST /api/v1/identity/auth/register-customer`.

#### [MODIFY] [Login.tsx](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/features/auth/components/Login.tsx)
- Add link/tab for Customer Registration (`/register`) and role-aware navigation after login (`ROLE_CUSTOMER` -> `/customer/active-session` or `/customer/profile`).

#### [NEW] [CustomerRegister.tsx](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/features/auth/components/CustomerRegister.tsx)
- Self-service customer registration component with name, email, password, and phone number fields.

---

### Customer Feature Modules (`src/features/customer`)

#### [NEW] [customerApi.ts](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/features/customer/api/customerApi.ts)
- `getCustomerProfile()`: Fetches customer details & generated account number.
- `getVehicles()` / `addVehicle()` / `delistVehicle()`: Interfacing with `/api/v1/vehicles`.
- `getActiveSession()` / `stopSession()`: Interfacing with `/api/v1/sessions/active` & `/api/v1/sessions/{id}/stop`.
- `getSessionHistory()` / `getInvoice()`: Interfacing with `/api/v1/sessions/customer/{id}` & `/api/v1/billing/invoices/session/{sessionId}`.

#### [NEW] [CustomerProfilePage.tsx](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/features/customer/pages/CustomerProfilePage.tsx)
- Displays customer account info, account number, contact details, and vehicle count summary.

#### [NEW] [VehicleManagementPage.tsx](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/features/customer/pages/VehicleManagementPage.tsx)
- Interactive grid/table of registered vehicles, modal to register new vehicle with license plate & RFID tag number, and status toggle/de-list actions.

#### [NEW] [ActiveSessionPage.tsx](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/features/customer/pages/ActiveSessionPage.tsx)
- Real-time gauge/card displaying active charging session, station & connector details, delivered kWh, duration timer, running total cost, and a prominent "Stop Charging Session" action button.

#### [NEW] [CustomerSessionsPage.tsx](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/features/customer/pages/CustomerSessionsPage.tsx)
- Searchable & filterable table of past charging sessions, total monthly expenditure metrics, and itemized invoice details modal.

---

### App Layout & Routing

#### [MODIFY] [Layout.tsx](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/app/layout/Layout.tsx)
- Adapt top navigation bar to render customer-specific menu items when logged in as `ROLE_CUSTOMER`.

#### [MODIFY] [App.tsx](file:///Users/thangnguyen/Workings/private/evchargingplatform/frontend/src/App.tsx)
- Register `/register`, `/customer/profile`, `/customer/vehicles`, `/customer/active-session`, and `/customer/sessions` routes.

---

## Verification Plan

### Automated Tests
- Run `npm test` or `vitest` in the `frontend/` directory to verify component rendering and state logic.

### Manual Verification
- Launch dev server (`npm run dev`) and test customer registration flow, vehicle registration, active session monitor, and session history/receipt modals.
