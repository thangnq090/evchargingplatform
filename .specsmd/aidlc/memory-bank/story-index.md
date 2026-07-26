# Global Story Index

## Overview
- **Total stories**: 0
- **Generated**: 0
- **Last updated**: "2026-07-24T15:00:00Z"

---

## Stories by Intent

### 001-ev-charging-mvp

#### unit: 001-identity-service
- [ ] **001-001**: Admin registration and login
- [ ] **001-002**: Vendor user registration and invitation
- [ ] **001-003**: Customer registration with account number
- [ ] **001-004**: JWT authentication with RS256 signing
- [ ] **001-005**: RBAC and credential management

#### unit: 002-station-management
- [ ] **002-001**: Chargepoint CRUD with location
- [ ] **002-002**: Chargepoint availability management
- [ ] **002-003**: Admin markup configuration
- [ ] **002-004**: Vendor-scoped chargepoint queries

#### unit: 003-session-management
- [ ] **003-001**: Charging session lifecycle (start/end)
- [ ] **003-002**: Meter reading recording
- [ ] **003-003**: Customer session history and monthly totals
- [ ] **003-004**: Vendor session report generation

#### unit: 004-billing-pricing
- [x] **004-001**: Cost calculation with marked-up rates
- [x] **004-002**: Invoice generation
- [x] **004-003**: Admin income reporting
- [ ] **004-004**: Vendor income insights

#### unit: 005-payment-processing
- [x] **005-001**: PaymentProvider interface and MockPayment adapter
- [x] **005-002**: Lightweight payment orchestrator workflow
- [ ] **005-003**: Idempotency and retry with backoff
- [ ] **005-004**: Compensation actions on failure

#### unit: 006-vehicle-management
- [ ] **006-001**: Vehicle registration with plate and RFID
- [ ] **006-002**: Vehicle de-listing and re-registration
- [ ] **006-003**: Vehicle lookup by plate/RFID

#### unit: 007-admin-portal
- [ ] **007-001**: Admin Dashboard API endpoints
- [ ] **007-002**: Vendor Dashboard API endpoints

#### unit: 008-session-search
- [x] **008-001**: PostgreSQL FTS index and search endpoint

#### unit: 009-notification
- [ ] **009-001**: Console log notification channel

#### unit: 010-device-gateway
- [ ] **010-001**: OCPP 1.6J WebSocket connection and auth
- [ ] **010-002**: OCPP message routing and event translation
- [ ] **010-003**: Heartbeat monitoring and session affinity

---

## Stories by Status

- **Planned**: 30
- **Generated**: 0
- **In Progress**: 0
- **Completed**: 0
