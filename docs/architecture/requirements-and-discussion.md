# EV Charging Platform – Requirements & Architecture Discussion Thread

**Date:** 2026-07-24
**Participants:** Product Owner, Architecture Team
**Format:** Grilling session (iterative decision interrogation)

---

## Part 1: Baseline Requirements (Provided as Context)

### Objective
Build a cloud-based platform to manage and operate electric vehicle (EV) charging stations. Central hub connecting administrators, charging station vendors, charging devices, customers, and vehicles.

### Functional Requirements
1. **User Management** – Registration, authentication, RBAC (Admin, Vendor, Customer), profile management
2. **Charging Station Management** – Registration, multi-vendor support, availability/health monitoring, connector config, remote start/stop/restart
3. **Charging Session Management** – Start/stop, real-time progress, duration/energy/status recording, history
4. **Customer Mobile App** – Find stations, view availability/pricing, QR/selection start, progress monitoring, history/receipts
5. **Payment & Billing** – Credit Card, Apple Pay, Google Pay; configurable pricing rules; secure processing; invoices/receipts
6. **Vendor Management** – Vendor registration, charger models, firmware versions, remote firmware updates
7. **Monitoring & Operations** – Real-time dashboard, offline/fault alerts, operational reporting
8. **Notification Service** – Customer (start/complete/fail), Operator (faults/critical events)

### Non-Functional Requirements
| Category | Requirement |
|----------|-------------|
| Scalability | Thousands of stations, millions of sessions/year |
| Availability | 99.9%, no single points of failure |
| Performance | Commands < 2s, status updates < 5s |
| Security | OAuth 2.0/OIDC, TLS everywhere, PCI DSS payments |
| Reliability | No lost sessions/payments, idempotent APIs |
| Maintainability | Modular microservice architecture, independent deployment, API-first |
| Observability | Centralized logging, metrics/dashboards, distributed tracing, real-time alerting |

### Primary Actors
- Administrator – Platform operations
- Vendor – Provides/maintains stations
- Customer – Uses mobile app to charge
- Charging Station – Physical device (OCPP)
- Vehicle – Receives charging
- Payment Provider – Processes payments

### Core Use Cases
1. Customer locates available station
2. Customer starts charging session
3. Station authenticates with platform
4. Platform monitors progress
5. Customer stops charging
6. Platform calculates fee
7. Payment processed
8. Receipt generated
9. Admin monitors health
10. Vendor updates firmware remotely

---

## Part 2: Grilling Session – Decision Log

### Question 1: Charging Station Communication Protocol

**Decision:** OCPP 1.6J over WebSocket with protocol abstraction layer

**Reasoning:**
- Industry de facto standard
- WebSocket provides bidirectional low-latency for 5s real-time requirement
- Abstraction layer enables future OCPP 2.0.1 migration without touching business domains
- Avoided proprietary vendor protocols (lock-in) and HTTP polling (latency)

---

### Question 2: Microservices Decomposition Strategy

**Decision:** Modular monolith with DDD boundaries (not microservices for v1)

**Module Boundaries:**
| Module | Core Responsibility |
|--------|---------------------|
| Identity & Access | AuthN/AuthZ, RBAC, profiles |
| Station Management | Stations, connectors, vendors, firmware |
| Session Management | Session lifecycle, metering, state |
| Pricing & Billing | Tariffs, cost calc, invoicing |
| Payment Processing | Payment orchestration, provider integration |
| Notification | Multi-channel delivery, templates |
| Device Gateway | OCPP protocol, message routing, device auth |

**Reasoning:**
- Aggressive timeline → distributed system overhead unjustified
- Clear module boundaries = extraction path preserved
- ArchUnit tests enforce compile-time boundaries
- Single deployment = simpler ops, debugging, testing

---

### Question 3: Data Architecture Within Modular Monolith

**Decision:** Single PostgreSQL, schema-per-module, no cross-schema joins

```
PostgreSQL
├── identity (schema)
├── station (schema)
├── session (schema)
├── billing (schema)
├── payment (schema)
├── notification (schema)
├── device_gateway (schema)
└── shared (reference only)
```

**Rules:**
- Module code only accesses its own schema (DB permissions + ArchUnit)
- Cross-module data = domain events + eventual consistency
- Foreign keys across schemas = read-only references
- Flyway migrations per module

---

### Question 4: Inter-Module Communication Pattern

**Decision:** Domain events + in-process event bus (transactional outbox pattern)

**Pattern:**
```
SessionMgmt: SessionStarted → EventBus
    → Pricing: reserveQuote()
    → Payment: authorizeHold()
    → DeviceGateway: sendStartTransaction()
```

**Key Principles:**
- Publishing module knows **zero** consumers
- Events published **after** local transaction commits
- Compensating events for failures (saga)
- No `@Transactional` spanning modules

---

### Question 5: Cross-Module Transaction Boundaries & Saga

**Decision:** Choreography-based saga with explicit compensation; orchestrator in Session module

**Flow:**
```
1. SessionCreated
2. QuoteReserved (Pricing)
3. HoldAuthorized (Payment)
4. StartSent (DeviceGateway)

ON FAILURE:
  - Payment: ReleaseHold
  - Pricing: ReleaseQuote
  - Session: MarkFailed
```

**Why not 2PC / synchronous calls:**
- 2PC couples to transaction manager, blocks on OCPP latency
- Sync calls = temporal coupling, thread blocking, extraction blocker

---

### Question 6: External API Contract Strategy

**Decision:** REST + OpenAPI 3.1 (sync), SSE (real-time)

| Client | Protocol | Real-time |
|--------|----------|-----------|
| Mobile App | REST | SSE |
| Admin Dashboard | REST | SSE |
| Vendor Portal | REST | SSE |
| Charging Stations | OCPP 1.6J (WS) | Native |

**Rejected:** GraphQL (overkill), gRPC (browser complexity), WebSocket for clients (SSE simpler for server→client only)

---

### Question 7: Authentication & Authorization

**Decision:** Spring Cloud Gateway + OAuth2 Resource Server (JWT validation), abstract IdP (Keycloak/Azure AD later)

**Token Flow:**
```
Client → IdP (OIDC) → JWT
    → Gateway (validation, token relay)
    → Module (local JWKS cache validation)
```

**Roles:** ADMIN, VENDOR, CUSTOMER
**Scopes:** Fine-grained (`station:read`, `session:start`, `billing:read`)

---

### Question 8: Billing & Payment Architecture

**Decision:** Separate modules; Billing = policy, Payment = provider adapters

**Pricing:** Pluggable `TariffStrategy` (energy, time, session, idle, dynamic)
**Money:** JSR 354 (`MonetaryAmount`) — never raw `BigDecimal`
**PCI:** Never touch PAN; provider tokenization only
**Saga:** SessionEnded → CalculateCost → ReserveFunds → CapturePayment → GenerateInvoice
**Idempotency:** `idempotency_key = session_id + action`

---

### Question 9: Session Completion vs Payment Settlement

**Decision:** **Decoupled** — Session completes; payment settles async

**Rationale:**
- Customer drives away immediately
- Payment retries (3D Secure, network issues) don't block charger
- Idempotency keys prevent double-charge
- Background reconciliation job handles edge cases

---

### Question 10: Observability Stack

**Decision:** OpenTelemetry everywhere → Grafana Stack (Loki, Mimir, Tempo, Grafana)

**Signals:**
- Logs: Structured JSON with `trace_id`, `span_id`, `session_id`, `station_id`
- Metrics: RED + business (active_sessions, online_chargers, payment_success_rate)
- Traces: OTel Java Agent auto-instrumentation + manual baggage propagation
- Alerts: PromQL rules → Alertmanager → PagerDuty/Slack

---

### Question 11: Rate Limiting & Abuse Protection

**Decision:** Gateway-level (Spring Cloud Gateway) + Device Gateway, extension points reserved

**MVP:** Global per-IP, per-user, per-station limits
**Future:** Business-rate-limiting (sessions/hour per customer), adaptive limits

---

### Question 12: Testing Strategy

**Decision:** Pyramid with module boundaries as test seams

| Layer | Scope | Tools |
|-------|-------|-------|
| Unit | Domain logic, strategies, mappers | JUnit, Mockito |
| Module Integration | Module ports + adapters (in-process) | Testcontainers (PG), Spring slice tests |
| Contract | OpenAPI spec vs implementation | springdoc-openapi + schemathesis |
| Protocol | OCPP message flows | Custom OCPP simulator (test fixture) |
| End-to-End | Critical paths (start→pay→invoice) | Postman/Newman in CI |

**Deferred:** Consumer-driven contract testing (Pact) — add at extraction phase

---

### Question 13: Disaster Recovery & Data Protection

**Decision:** Managed PG automated backups + point-in-time recovery + immutable audit log

**RPO/RTO:**
- Transactional data (sessions, payments): RPO < 1 min (PITR), RTO < 30 min
- Reference data (stations, users): RPO < 1 hr, RTO < 4 hr
- Audit log: Immutable, never deleted, cross-region replication

**Testing:** Quarterly restore drills; annual failover exercise

---

### Question 14: Deployment Topology

**Decision:** Single-region multi-AZ, Kubernetes (managed), Helm, rolling updates

**Infrastructure:**
- PostgreSQL: Cloud SQL / RDS (multi-AZ)
- Redis: ElastiCache / Memorystore (multi-AZ)
- Object Storage: S3 / GCS (firmware, invoices)
- Ingress: Nginx + cert-manager

**Environments:** dev (per-PR), staging (prod mirror), prod

**GitOps Path:** ArgoCD + Kustomize when team/frequency justifies

---

### Question 15: CI/CD Pipeline

**Decision:** Build → Test → Scan → Helm deploy; GitOps-ready

**Stages:**
1. PR: Build → Unit → Integration → Contract (OpenAPI) → Trivy scan
2. Merge: Build multi-arch image → Sign (cosign) → SBOM (Syft) → Push GHCR/ECR
3. Deploy: Helm upgrade with health checks + rollback
4. Promote: Tag → staging → prod (manual gate)

**Secrets:** External Secrets Operator + AWS Secrets Manager

---

### Question 16: Multi-Tenancy (Vendor Isolation)

**Decision:** Single platform, vendor-scoped data, RLS-ready

```sql
ALTER TABLE stations ENABLE ROW LEVEL SECURITY;
CREATE POLICY vendor_isolation ON stations
  USING (vendor_id = current_setting('app.current_vendor_id')::uuid);
```

**Vendor Portal:** JWT `vendor_id` claim → RLS context
**Admin Portal:** Global view (bypass RLS)
**White-label:** Separate deployment per tenant (not schema isolation)

---

### Question 17: Charging Session State Machine

**Decision:** Simplified v1; clear ownership split

| Layer | State Responsibility |
|-------|---------------------|
| Device Gateway | OCPP protocol state (StartTransaction.conf, StopTransaction.req, MeterValues) |
| Session Management | Business lifecycle: PENDING → CHARGING → COMPLETED → INVOICED |

**Events from Gateway:** `ChargingStarted`, `MeterValueReceived`, `ChargingStopped`
**Session Mgmt:** Consumes, transitions, publishes `SessionCompleted`

**Deferred to v2:** Full reconciliation, sophisticated timeout guards, clock drift handling

---

### Question 18: OCPI / Roaming

**Decision:** **Not in v1 scope** — closed network only

**Future:** OCPI 2.2 module for Hubject/Gireve/PlugSurfing interoperability when business requires

---

### Question 19: Firmware Orchestration

**Decision:** **Vendor self-serve in v1** — platform stores metadata, vendor triggers via portal

**Scope:**
- Vendor uploads firmware artifact → S3
- Platform records version, compatibility (charger model)
- Vendor initiates rollout (target station group, schedule)
- Platform sends OCPP `UpdateFirmware` via Device Gateway
- Station reports result → platform records status

**Deferred:** Staged rollout (canary), automatic rollback on failure rate, device-side verification

---

### Question 20: Implementation Kickoff

**Decision:** Move to implementation planning

**Next Steps:**
1. Enter Plan Mode → scaffold module structure
2. Define domain events (Avro/JSON schema)
3. Generate Flyway baseline migrations per module
4. Set up CI pipeline (build, test, scan, Helm)
5. Provision dev Kubernetes + managed PG/Redis
6. Implement Device Gateway OCPP server (Spring WebFlux)
7. Implement Session Management + Saga orchestrator
8. Implement Billing + Payment adapters (Stripe first)

---

## Part 3: Open Questions for Future Sessions

1. **OCPI/Roaming** — When business requires network interoperability
2. **Smart Charging / ISO 15118** — Vehicle-to-grid, dynamic load balancing
3. **Advanced Firmware Orchestration** — Canary, rollback, attestation
4. **Analytics / Data Lake** — Session analytics, revenue recognition, carbon reporting
5. **White-label Deployment Automation** — Tenant onboarding pipeline

---

## Appendix: Decision Principles Applied

| Principle | Application |
|-----------|-------------|
| **Defer complexity until proven** | No Temporal, no GraphQL, no canary, no multi-region — yet |
| **Extract, don't rewrite** | Every module boundary is an extraction seam |
| **Protocol boundaries are sacred** | Device Gateway owns OCPP; business domains never see frames |
| **Money is never a float** | JSR 354 everywhere |
| **PCI scope by design** | Payment adapters only touch tokens |
| **Observability is not optional** | OTel from commit #1 |
| **Test at the boundary** | Module integration tests = contract tests |
| **Operate what you build** | Team owns deployment, monitoring, on-call |

---