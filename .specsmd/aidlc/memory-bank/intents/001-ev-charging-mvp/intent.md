# Intent: EV Charging Platform MVP

## Overview
Build a cloud-based platform to manage and operate electric vehicle (EV) charging stations as defined in the MVP requirements. The platform connects administrators, charging station vendors, charging devices, customers, and vehicles, enabling secure, reliable, and scalable charging services.

**Source Documents:**
- `/docs/requirement/mvp-requirement.md` - Detailed entity model and functional requirements
- `/docs/architecture/transcript.md` - Architectural decisions from grilling session

## Intent ID
`001-ev-charging-mvp`

## Status
**In Progress** - Inception phase started

## Primary Actors (from requirements)

| Actor | Description |
|-------|-------------|
| **Administrator** | Operates and manages the entire platform; signs up vendors, views income, sets markup, resets credentials |
| **Vendor** | Provides and maintains charging stations; manages chargepoints, views income/activity reports |
| **Vendor User** | Belongs to a vendor; manages vendor-owned resources per assigned permissions |
| **Customer** | Registers with platform; performs charging sessions; views history; manages vehicles |
| **Charging Station (Device)** | Physical charger communicating via OCPP 1.6J over WebSocket |
| **Vehicle | Identified by RFID or plate; owned by customer; can change ownership |
| **Payment Provider** | External payment processing (Stripe, Adyen, etc.) |

## Key Functional Requirements (from MVP Requirements)

### Admin Capabilities
- Sign up vendors (including inviting original vendor user)
- View income over a period, optionally filtered by vendor
- Set the markup added to the vendor's unit price
- Reset credentials for users where appropriate

### Vendor Capabilities
- Manage vendor-owned resources (chargepoints) per assigned permissions
- Add, update, remove chargepoints
- View income and charging activity (current month + breakdowns by chargepoint over recent days/weeks/months)
- Generate reports of charging sessions for a chargepoint on a given date

### Vendor User Capabilities
- Belong to exactly one vendor
- Manage vendor-owned resources per assigned permissions

### Chargepoint Properties
- Unique identifier (displayed on physical chargepoint)
- Group label (for vendor to identify groups of chargepoints)
- Unit price in tenths of cents
- Availability status (may be temporarily unavailable)

### Customer Capabilities
- Register with platform (name, email, account number, phone number)
- Perform charging sessions using marked-up unit price
- View charging session history and totals by month (including current partial month)
- Own one or more vehicles; maintain them; de-list vehicles
- Vehicles can be re-registered with same plate by different customers

### Vehicle Properties
- Registration plate
- RFID number
- Customer owner
- Can be identified automatically (RFID/plate detection) or manually selected
- Manual selection with RFID available → associate RFID for future identification

### Charging Session Properties
- Start time, end time
- Vehicle, chargepoint
- Marked-up unit rate (price at session start time)
- Error code (empty if successful)
- Total energy delivered (kWh)
- Total amount charged to customer
- Belongs to the month in which it starts

### Full-Text Search (Admin)
- Search charging sessions by session, customer, or vehicle info
- Partial matches (e.g., "AUD" matches plates "AUD186" and "AUD994")
- Searchable fields: registration plates, customer account numbers, error codes

## Non-Functional Requirements (from transcript & ADR-0001)

| Requirement | Target |
|-------------|--------|
| **Scalability** | Thousands of charging stations across multiple regions; millions of sessions annually |
| **Availability** | 99.9% |
| **Performance** | Charging commands < 2s; real-time charger status updates < 5s |
| **Security** | OAuth 2.0 / OIDC; TLS everywhere; PCI DSS for payments |
| **Reliability** | No lost sessions/payments; idempotent APIs for critical operations |
| **Observability** | OpenTelemetry → Grafana stack (Loki, Mimir, Tempo); correlation IDs |
| **Deployment** | Single-region multi-AZ; containerized; Helm; managed PostgreSQL/Redis |
| **Multi-tenancy** | Vendor-scoped data with RLS; single platform; white-label via separate deployment |

## Architectural Decisions (Locked from Transcript/ADR-0001)

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Protocol** | OCPP 1.6J over WebSocket with protocol abstraction layer | Industry standard; meets 5s real-time; abstraction enables OCPP 2.0.1 migration |
| **Architecture** | Modular monolith with DDD boundaries & hexagonal ports/adapters | Fast delivery; avoids distributed complexity; clear extraction path to microservices |
| **Data** | Single PostgreSQL, schema-per-module, no cross-schema joins | Clear ownership; simple ops; easy extraction to separate DBs later |
| **Module Communication** | In-process domain events + lightweight orchestrator | Decoupled modules; saga-ready; no temporal coupling |
| **External API** | REST + OpenAPI 3.1 for sync, SSE for real-time | Simple, widely adopted; works for mobile/web/vendor portals |
| **Auth** | Spring Cloud Gateway + Resource Server (JWT), abstract IdP | No Keycloak ops burden; standards-compliant; swap IdP later |
| **Device Gateway** | Dedicated module, OCPP → domain events, reactive-ready | Protocol boundary isolated; scalable to WebFlux/Netty when needed |
| **Billing/Payment** | Separate modules; provider tokenization; saga with compensation | PCI scope reduction; async settlement; decoupled from session lifecycle |
| **Session/Payment Decoupling** | Session completes; payment settles async with idempotency | UX: customer drives away; reliability: retries without blocking charger |
| **Observability** | OpenTelemetry → Grafana stack (Loki, Mimir, Tempo) | Vendor-neutral; single query language; auto-instrumentation |
| **Rate Limiting** | Gateway + Device Gateway basics, extension points | MVP protection; domain-specific limits added as patterns emerge |
| **Testing** | Unit + module integration + OpenAPI validation + OCPP simulator | Fast feedback; contract testing deferred to extraction phase |
| **Disaster Recovery** | Managed PG backups + immutable audit log, tested restores | Protects transactional data; RPO/RTO defined; recovery tested |
| **Deployment** | Single-region multi-AZ, Helm, rolling updates | Reliable HA; low ops complexity; GitOps/canary path preserved |
| **CI/CD** | Build → Test → Scan → Helm deploy, GitOps-ready | Secure supply chain; progressive delivery when justified |
| **Multi-Tenancy** | Vendor-scoped data, RLS-ready, single platform | Low ops overhead; white-label path via deployment isolation |
| **Session State** | Simplified v1; protocol state (Gateway) vs business state (Session) | Fast MVP; explicit ownership; sophisticated state machine later |

## Module Boundaries (8 modules per ADR-0001 + Vehicle Management)

| Module | Responsibility | Key Aggregates |
|--------|----------------|----------------|
| **Identity & Access** | AuthN/AuthZ, RBAC, user profiles | User, Role, Permission |
| **Station Management** | Station registry, connectors, health, firmware metadata | Station, Connector, Firmware |
| **Session Management** | Charging session lifecycle, metering, state machine | ChargingSession, MeterReading |
| **Pricing & Billing** | Tariff rules, cost calculation, invoicing | Tariff, Invoice, BillingAccount |
| **Payment Processing** | Payment orchestration, provider integration | Payment, PaymentMethod |
| **Notification** | Multi-channel delivery, templates, preferences | Notification, Template, Channel |
| **Device Gateway** | OCPP protocol handling, message routing, device auth | DeviceConnection, OcppMessage |
| **Vehicle Management** | Vehicle entity lifecycle, RFID, ownership, de-registration | Vehicle, OwnershipTransfer |

## Out of Scope for MVP (Deferred per Transcript)
- Full firmware management (metadata only)
- Sophisticated session state machine (simplified v1)
- Contract testing (deferred to extraction phase)
- Full reconciliation jobs
- Advanced timeout guards, clock drift handling
- White-label multi-tenancy (single platform with RLS)
- GraphQL / gRPC APIs
- Separate databases per module (single PG with schemas)
- Email/SMS/Push notifications (console log only for MVP)
- External search engine (PostgreSQL FTS for MVP; OpenSearch later if needed)

## Unit Decomposition Principles

Per architecture rules, every business capability is owned by **exactly one unit**:

| Principle | Application |
|----------|-------------|
| **Single Ownership** | No business logic or persistent data duplicated across units |
| **Public API Access** | Unit requiring info from another accesses only via public API or domain events |
| **No Cross-Unit Direct Access** | No direct repository access, entity manipulation, or cross-module transactions |
| **Event-Driven** | Modules communicate via domain events (SessionCompletedEvent, etc.) |

### Unit-to-Module Mapping

| Unit | Owning Module | Responsibility |
|------|---------------|----------------|
| `001-identity-service` | Identity & Access | User auth, registration, RBAC, credential reset |
| `002-station-management` | Station Management | Chargepoint CRUD, availability, group labels |
| `003-session-management` | Session Management | Charging sessions, meter readings, lifecycle |
| `004-billing-pricing` | Pricing & Billing | Tariffs, markup, cost calculation, invoicing |
| `005-payment-processing` | Payment Processing | Provider abstraction, MockPayment adapter (MVP) |
| `006-vehicle-management` | **Vehicle Management** | Vehicle entity lifecycle: registration, RFID, ownership, de-registration |
| `007-admin-portal` | Application/UI Layer | Aggregates data from Station/Billing/Session modules (no business logic ownership) |
| `008-session-search` | Session Management | PostgreSQL Full-Text Search for sessions |
| `009-notification` | Notification | Console log only (MVP); defer Email/SMS/Push |
| `010-device-gateway` | Device Gateway | OCPP 1.6J WebSocket, charger communication |

### Key Decisions (Confirmed)

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Vehicle Management** | Separate module | First-class entity with own lifecycle; avoids coupling customer/charging logic |
| **Admin Portal** | Application/UI layer | Aggregates from Station/Billing/Session; no business logic ownership |
| **Device Gateway** | Dedicated module (ADR-007) | Protocol boundary isolation per architecture |
| **Notifications** | Console log only (MVP) | Defer Email/SMS/Push until business requirement |
| **Payment Provider** | Abstract + MockPayment adapter | PaymentProvider interface; Stripe/Adyen adapters later |
| **Full-Text Search** | PostgreSQL FTS (MVP) | Satisfies requirements without extra infrastructure; OpenSearch migration path defined |

## Acceptance Criteria for Inception Complete

- [ ] Requirements documented (FR/NFR)
- [ ] System context mapped
- [ ] Units decomposed (one per module + cross-cutting)
- [ ] User stories created with acceptance criteria
- [ ] Bolts planned for Construction phase
- [ ] Inception review passed

## Next Steps
1. Gather detailed requirements (FR/NFR) per module
2. Define system context diagram
3. Decompose into units
4. Create user stories per unit
5. Plan bolts for construction