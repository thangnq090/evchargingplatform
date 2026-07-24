---
intent: 001-ev-charging-mvp
phase: inception
status: draft
created: "2026-07-24T15:00:00Z"
updated: "2026-07-24T15:00:00Z"
---

# Requirements: EV Charging Platform MVP

## Intent Overview
Build a cloud-based platform MVP to manage and operate electric vehicle (EV) charging stations. The platform connects administrators, charging station vendors, charging devices, customers, and vehicles, enabling secure, reliable charging services.

## Business Goals

| Goal | Success Metric | Priority |
|------|----------------|----------|
| Enable Admin management of platform | Admin can sign up vendors, view income, set markup | Must |
| Enable Vendor chargepoint management | Vendor can add/update/remove chargepoints, view activity | Must |
| Enable Customer charging | Customer can register, perform sessions, view history | Must |
| Support vehicle identity | Vehicle identified by RFID/plate, ownership tracked | Must |
| Provide charging session search | Admin can search sessions by customer/vehicle info | Must |
| Reliable payment settlement | Payment settled asynchronously with idempotency | Must |

---

## Functional Requirements

### Identity & Access

#### FR-1: Admin User Registration
- **Description**: Admin users can be created with name and email. Admin can sign up vendors and invite the original vendor user.
- **Acceptance Criteria**:
  - Admin registration via email + password
  - Admin can create a vendor account
  - Admin receives confirmation of vendor creation
  - Original vendor user receives invitation
- **Priority**: Must
- **Related Stories**: TBD

#### FR-2: Vendor User Management
- **Description**: Vendors have one or more users who can manage vendor-owned resources according to their assigned permissions.
- **Acceptance Criteria**:
  - Vendor users belong to exactly one vendor
  - RBAC with roles: ADMIN, VENDOR_ADMIN, VENDOR_USER
  - Permissions scoped to vendor's resources
  - Admin can reset credentials for any user
- **Priority**: Must
- **Related Stories**: TBD

#### FR-3: Customer Registration
- **Description**: Customers register with the platform providing name, email, account number (auto-generated), and phone number.
- **Acceptance Criteria**:
  - Customer registers with name, email, phone number
  - Account number auto-generated on registration
  - Customer can log in with email + password
  - Customer profile stores: name, email, account number, phone
- **Priority**: Must
- **Related Stories**: TBD

#### FR-4: Authentication & Authorization
- **Description**: JWT-based authentication with Spring Cloud Gateway + Resource Server. Roles: ADMIN, VENDOR_ADMIN, VENDOR_USER, CUSTOMER. JWT access tokens are cryptographically signed by the Authentication Service and validated by all downstream services.
- **Acceptance Criteria**:
  - Login returns JWT access token (short-lived) + refresh token
  - JWT access tokens signed using RS256 (RSA SHA-256) or ES256 (ECDSA SHA-256)
  - **Authentication Service** signs JWT using private key
  - **API Gateway / Resource Servers** validate JWT signature using public key
  - Tokens rejected for: invalid signature, expired timestamp, invalid issuer, invalid audience, missing required claims
  - JWT contains: roles, vendor_id (for VENDOR_ADMIN and VENDOR_USER), sub, iat, exp, iss, aud
  - Role-based access enforced on all API endpoints
  - Refresh token rotation with reuse detection
  - Machine-to-machine auth via client credentials flow
- **Priority**: Must
- **Related Stories**: TBD

### Station Management

#### FR-5: Chargepoint Management
- **Description**: Vendors can add, update, and remove chargepoints. Each chargepoint has a unique identifier, group label, unit price (in tenths of cents), availability status, and geospatial location coordinates. Location data is captured from day one to enable future proximity search, mapping, and route planning without data migration.
- **Acceptance Criteria**:
  - Create chargepoint: unique ID, group label, unit price, vendor assignment, location (latitude + longitude)
  - Update chargepoint: name, group label, unit price, availability, location
  - Remove chargepoint: soft-delete (historical sessions preserved)
  - Chargepoint availability toggle: AVAILABLE, UNAVAILABLE, MAINTENANCE
  - Chargepoints scoped to owning vendor
  - Unit price stored in tenths of cents (integer)
  - **Location**: latitude (decimal degrees) + longitude (decimal degrees), stored as PostgreSQL `GEOGRAPHY(Point, 4326)` using PostGIS extension
  - **Future-ready**: PostGIS spatial index enables proximity queries, geo-fencing, and map rendering without schema migration
- **Priority**: Must
- **Related Stories**: TBD

#### FR-6: Admin Markup Configuration
- **Description**: Admin can set the markup percentage added to vendor's unit price for customer charging.
- **Acceptance Criteria**:
  - Admin can set markup per vendor
  - Markup applied to unit price at session start time
  - Markup history tracked for audit
- **Priority**: Must
- **Related Stories**: TBD

### Session Management

#### FR-7: Charging Session Lifecycle
- **Description**: Customers perform charging sessions using the marked-up unit price. Session records start time, end time, chargepoint, vehicle, error code, total energy (kWh), and total amount charged.
- **Acceptance Criteria**:
  - Session starts at a chargepoint with a vehicle
  - Marked-up unit rate captured at session start time
  - Session records: start, end, chargepoint, vehicle, unit rate, energy (kWh), amount charged ($), error code
  - Error code empty if session successful
  - Session belongs to the month in which it starts
- **Priority**: Must
- **Related Stories**: TBD

#### FR-8: Session History & Monthly Totals
- **Description**: Customers can view their charging session history and totals by month (including current partial month). Vendors can view session activity for their chargepoints.
- **Acceptance Criteria**:
  - Customer sees own sessions grouped by month
  - Monthly totals: sessions count, total energy, total charged
  - Current partial month included in listing
  - Sessions sorted by start time (newest first)
- **Priority**: Must
- **Related Stories**: TBD

#### FR-9: Vendor Session Reports
- **Description**: Vendors can generate reports of charging sessions for a chargepoint on a given date.
- **Acceptance Criteria**:
  - Input: chargepoint ID + date
  - Output: list of sessions with vehicle, start/end time, energy, amount, error code
  - Report downloadable or viewable in portal
- **Priority**: Should
- **Related Stories**: TBD

### Vehicle Management

#### FR-10: Vehicle Registration
- **Description**: Customers own one or more vehicles. Vehicles are identified by registration plate and RFID number. A vehicle may be re-registered with the same plate by a different customer after de-listing.
- **Acceptance Criteria**:
  - Customer registers vehicle: plate + RFID (optional)
  - Vehicle assigned to customer owner
  - Same plate can be registered by different customer after de-listing
  - Vehicle can be identified automatically by RFID or plate detection
  - Manual selection associates RFID if available
- **Priority**: Must
- **Related Stories**: TBD

#### FR-11: Vehicle De-listing
- **Description**: Customers can de-list vehicles (e.g., following disposal or sale). De-listed vehicles can be re-registered by another customer.
- **Acceptance Criteria**:
  - Customer can de-list own vehicle (soft-delete)
  - De-listed vehicle no longer appears in customer's vehicle list
  - Historical sessions preserved (vehicle data immutable)
  - De-listed vehicle can be re-registered with same plate by different customer
- **Priority**: Must
- **Related Stories**: TBD

### Billing & Payment

#### FR-12: Income Reporting (Admin)
- **Description**: Admin can view income over a period, optionally filtered by vendor.
- **Acceptance Criteria**:
  - Income by date range (start, end)
  - Optional vendor filter
  - Shows: total revenue, session count, average per session
  - Breakdown by vendor when no filter applied
- **Priority**: Must
- **Related Stories**: TBD

#### FR-13: Vendor Income & Activity Insights
- **Description**: Vendors can view income and charging activity, including current month total and breakdowns by chargepoint over recent days, weeks, and months.
- **Acceptance Criteria**:
  - Current month: total revenue, session count, energy delivered
  - Breakdown by chargepoint over last 7 days, 30 days, 12 months
  - Vendor sees only own data
- **Priority**: Should
- **Related Stories**: TBD

#### FR-14: Payment Orchestration
- **Description**: Payment is processed asynchronously after session completion using a simple asynchronous workflow orchestrator. The orchestrator is intentionally kept lightweight for MVP with clear module boundaries so it can evolve into a distributed Saga managed by Temporal when the platform scales. Payment provider abstraction with MockPayment adapter for MVP.
- **Acceptance Criteria**:
  - Session completes → payment workflow triggered via domain event
  - Lightweight orchestrator (not a full Saga framework) coordinates the flow:
    1. CalculateCostCommand → CostCalculatedEvent
    2. ReserveFundsCommand → FundsReservedEvent
    3. CapturePaymentCommand → PaymentCapturedEvent
    4. GenerateInvoiceCommand → InvoiceGeneratedEvent
  - Each step is an independent local transaction — no cross-module `@Transactional`
  - Compensation actions defined for each failure scenario:
    - ReserveFailed → release quote, mark session billing_failed
    - CaptureFailed → void authorization, retry with backoff
    - All compensations tracked in orchestrator state
  - Amount calculated from energy × marked-up unit rate + any fees
  - PaymentProvider interface with authorize, capture, refund, void
  - MockPayment adapter for MVP (simulates successful payment)
  - Idempotency keyed by session_id + action
  - Payment failure → retry with exponential backoff
  - **Future-ready boundaries**: Orchestrator module isolated behind a port interface — can be replaced by Temporal (workflow engine) without changing domain logic
  - **No distributed Saga framework for MVP** — orchestrator is in-process, application-level code with explicit state tracking
- **Priority**: Must
- **Related Stories**: TBD

#### FR-15: Payment/Session Decoupling
- **Description**: Charging session lifecycle and payment settlement lifecycle are decoupled. Session completes → payment settles asynchronously.
- **Acceptance Criteria**:
  - Customer can stop session and leave immediately
  - Payment settles in background with retries
  - No charging session blocked by payment processing
- **Priority**: Must
- **Related Stories**: TBD

### Session Search

#### FR-16: Full-Text Search
- **Description**: Admin can search charging sessions using session, customer, or vehicle information (registration plates, customer account numbers, error codes). Supports partial matches.
- **Acceptance Criteria**:
  - Search endpoint accessible to ADMIN role (only)
  - Searchable fields: registration plate, customer account number, error code, session ID
  - Partial match: "AUD" matches "AUD186" and "AUD994"
  - Results include: session details, customer info, vehicle info
  - Powered by PostgreSQL Full-Text Search
- **Priority**: Must
- **Related Stories**: TBD

### Device Gateway

#### FR-17: OCPP 1.6J WebSocket Communication
- **Description**: Physical charging stations communicate via OCPP 1.6J over WebSocket. Device Gateway handles protocol translation and publishes domain events.
- **Acceptance Criteria**:
  - Secure WebSocket (WSS) endpoint for charger connections
  - Device authentication via OCPP Authorize
  - Protocol abstraction layer (future OCPP 2.0.1 support without touching domain)
  - Translates OCPP frames → domain events (ChargingStarted, MeterValueReceived, ChargingStopped)
  - Handle charger heartbeat, status notifications
- **Priority**: Must
- **Related Stories**: TBD

### Notification

#### FR-18: Console Log Notifications
- **Description**: Notifications logged to console for MVP. No external delivery channels.
- **Acceptance Criteria**:
  - Notification events published to event bus
  - Console logger prints notification payload
  - Events: session start, session complete, payment succeeded, payment failed
  - Channel abstraction ready for future Email/SMS/Push adapters
- **Priority**: Could
- **Related Stories**: TBD

### Admin Portal

#### FR-19: Admin Dashboard
- **Description**: Admin dashboard as application/UI layer aggregating data from Station, Billing, Session, Identity modules.
- **Acceptance Criteria**:
  - View all vendors and their chargepoints
  - View system-wide income with date range + vendor filter
  - Set vendor markup
  - Reset user credentials
  - Access full-text search
  - No business logic ownership — purely aggregation/display
- **Priority**: Should
- **Related Stories**: TBD

### Vendor Portal

#### FR-20: Vendor Dashboard
- **Description**: Vendor portal showing vendor's chargepoints, income, activity, and session reports.
- **Acceptance Criteria**:
  - List own chargepoints with status
  - Add/update/remove chargepoints
  - View income (current month + breakdowns)
  - Generate session reports by chargepoint + date
  - Data filtered by vendor_id from JWT
- **Priority**: Should
- **Related Stories**: TBD

---

## Non-Functional Requirements

### Performance

| Requirement | Metric | Target | Priority |
|-------------|--------|--------|----------|
| API Response Time | p95 latency | < 200ms | Must |
| Charging Start Command | End-to-end latency | < 2s | Must |
| Real-time Status Update | Charger → Dashboard | < 5s | Must |
| Search Query Time | p95 latency | < 500ms | Must |
| Throughput | Requests/second | > 1000 | Should |

### Scalability

| Requirement | Metric | Target | Priority |
|-------------|--------|--------|----------|
| Concurrent Chargers | WebSocket connections | 10,000+ | Must |
| Sessions | Annual volume | 1M+ | Should |
| Stations | Registered | 5,000+ | Should |
| Concurrent Users | API users | 1,000 | Should |

### Security

| Requirement | Standard | Notes | Priority |
|-------------|----------|-------|----------|
| Authentication | OAuth 2.0 / OIDC | JWT tokens (RS256/ES256 signed), refresh rotation | Must |
| Authorization | RBAC | Roles: ADMIN, VENDOR_ADMIN, VENDOR_USER, CUSTOMER | Must |
| JWT Signing | RS256 or ES256 (asymmetric) | Auth Service signs with private key; Gateway/Resource Servers validate with public key | Must |
| JWT Validation | Claims: signature, exp, iss, aud, sub, roles | Reject invalid/expired tokens | Must |
| API Protection | Rate limiting | Gateway + Device Gateway basics | Must |
| Transport Security | TLS 1.3 | All endpoints | Must |
| Device Auth | OCPP Authorize + X.509 mTLS | Charger identity verification | Must |
| Payment Security | Provider tokenization | Never store PAN; payment_method_id only | Must |
| Password Storage | BCrypt or Argon2 | Hashed, salted | Must |

### Reliability

| Requirement | Metric | Target | Priority |
|-------------|--------|--------|----------|
| Availability | Uptime | 99.9% | Must |
| Data Durability | No session/payment loss | Exactly-once processing | Must |
| Idempotency | Retry safety | Idempotency key on all mutations | Must |
| Recovery | RTO | < 30 min | Must |
| Data Loss | RPO | < 1 min (transactional) | Must |

### Observability

| Requirement | Tool | Notes | Priority |
|-------------|------|-------|----------|
| Logging | Structured JSON (Logstash) | Include trace_id, span_id, session_id | Must |
| Metrics | Micrometer + Prometheus | RED metrics + business metrics | Should |
| Tracing | OpenTelemetry | Auto-instrument + manual spans | Should |
| Alerts | PromQL rules | Charger offline, payment failure | Should |
| Correlation IDs | All requests/sessions/events | End-to-end traceability | Must |

## Constraints

### Technical Constraints

**Project-wide standards**: Project standards loaded from memory-bank standards folder by Construction Agent.

**Intent-specific constraints**:
- Modular monolith architecture (single deployable unit)
- Single PostgreSQL instance, schema-per-module
- In-process domain events (no external message broker for MVP)
- No external IdP (Spring Security JWT validation, abstract IdP interface)
- Console log notifications only (no email/SMS/push)
- PostgreSQL Full-Text Search (no Elasticsearch/OpenSearch for MVP)
- OCPP 1.6J over WebSocket (protocol abstraction layer for future upgrades)
- Containerized + Helm deployment (Kubernetes-ready)
- Flyway migrations per module

### Business Constraints
- Aggressive delivery timeline (MVP within weeks, not months)
- Single-region multi-AZ deployment (no multi-region for MVP)
- PCI DSS compliance for payment workflows
- No firmware management in MVP scope
- Simplified session state machine v1 (reconciliation deferred)

## Assumptions

| Assumption | Risk if Invalid | Mitigation |
|------------|-----------------|-------------|
| Vendor has their own pricing strategy | Vendor cannot differentiate pricing | Vendor-specific markup + unit price per chargepoint |
| Customer pays marked-up price (vendor price + admin markup) | Pricing model needs rework | Transparent pricing breakdown in session details |
| Charger communicates reliably via OCPP 1.6J WebSocket | Charger uses proprietary protocol | Protocol abstraction layer; adapter for custom protocols |
| Payment processed asynchronously | Customer expects instant payment | Clear UX showing "payment processing" status |
| Single-region deployment sufficient | Latency/cross-region requirements | Application layer region-independent; multi-region path defined |
| PostgreSQL FTS sufficient for MVP | Search volume/complexity exceeds PG FTS | Migration path to OpenSearch/Elasticsearch defined |

## Open Questions

| Question | Owner | Due Date | Resolution |
|----------|-------|----------|------------|
| Specific payment provider for post-MVP? | Team | TBD | MockPayment for MVP; provider decision deferred |
| Exact OCPP library to use? | Team | Construction start | Evaluate ocpp-spring-boot-starter vs steve-community |
| Admin portal tech stack? | Team | Construction start | REST API + Swagger for MVP; dedicated UI later |
| Email server for future notifications? | Team | Post-MVP | Deferred |
| Multi-region deployment timeline? | Team | Post-MVP | Single-region MVP; 2027 target for multi-region |

---

## Priority Definitions

| Priority | Meaning |
|----------|---------|
| **Must** | Required for MVP, system unusable without |
| **Should** | Important, significant value but not blocking |
| **Could** | Nice to have, enhances experience |
| **Won't** | Out of scope for this intent |

---

## Requirement Quality Checklist

- [x] All requirements are testable (measurable, not vague)
- [x] Acceptance criteria are binary (pass/fail)
- [x] NFRs have specific metrics and targets
- [x] Dependencies are identified
- [x] Constraints are documented
- [x] Assumptions are stated and risks assessed