# Architecture Decision Record: EV Charging Platform

**Date:** 2026-07-24
**Status:** Accepted
**Authors:** Architecture Team

---

## Context

Build a cloud-based platform to manage and operate electric vehicle (EV) charging stations. The platform connects administrators, charging station vendors, charging devices, customers, and vehicles, enabling secure, reliable, and scalable charging services.

**Key Constraints:**
- Aggressive delivery timeline
- Thousands of charging stations across multiple regions
- Millions of charging sessions annually
- 99.9% availability target
- PCI DSS compliance for payments

---

## Decision Summary

| # | Decision Area | Choice | Rationale |
|---|---------------|--------|-----------|
| 1 | **Charging Station Protocol** | OCPP 1.6J over WebSocket with protocol abstraction layer | Industry standard; WebSocket meets 5s real-time requirement; abstraction enables OCPP 2.0.1 migration |
| 2 | **Architecture Style** | Modular monolith with DDD boundaries & hexagonal ports/adapters | Fast delivery; avoids distributed system complexity; clear extraction path to microservices |
| 3 | **Data Architecture** | Single PostgreSQL, schema-per-module, no cross-schema joins | Clear ownership; simple operations; easy extraction to separate DBs later |
| 4 | **Inter-Module Communication** | In-process domain events + lightweight orchestrator | Decoupled modules; saga-ready; no temporal coupling |
| 5 | **External API** | REST + OpenAPI 3.1 for sync, SSE for real-time | Simple, widely adopted; works for mobile/web/vendor portals |
| 6 | **Authentication** | Spring Cloud Gateway + Resource Server (JWT), abstract IdP | No Keycloak ops burden; standards-compliant; swap IdP later |
| 7 | **Device Gateway** | Dedicated module, OCPP → domain events, reactive-ready | Protocol boundary isolated; scalable to WebFlux/Netty when needed |
| 8 | **Billing & Payment** | Separate modules; provider tokenization; saga with compensation | PCI scope reduction; async settlement; decoupled from session lifecycle |
| 9 | **Session/Payment Decoupling** | Session completes; payment settles async with idempotency | UX: customer drives away; reliability: retries without blocking charger |
| 10 | **Observability** | OpenTelemetry → Grafana stack (Loki, Mimir, Tempo) | Vendor-neutral; single query language; auto-instrumentation |
| 11 | **Rate Limiting** | Gateway + Device Gateway basics, extension points | MVP protection; domain-specific limits added as patterns emerge |
| 12 | **Testing Strategy** | Unit + module integration + OpenAPI validation + OCPP simulator | Fast feedback; contract testing deferred to extraction phase |
| 13 | **Disaster Recovery** | Managed PG backups + immutable audit log, tested restores | Protects transactional data; RPO/RTO defined; recovery tested |
| 14 | **Deployment** | Single-region multi-AZ, Helm, rolling updates | Reliable HA; low ops complexity; GitOps/canary path preserved |
| 15 | **CI/CD** | Build → Test → Scan → Helm deploy, GitOps-ready | Secure supply chain; progressive delivery when justified |
| 16 | **Multi-Tenancy** | Vendor-scoped data, RLS-ready, single platform | Low ops overhead; white-label path via deployment isolation |
| 17 | **Charging Session State** | Simplified v1; protocol state (Gateway) vs business state (Session) | Fast MVP; explicit ownership; sophisticated state machine later |

---

## Detailed Decisions

### ADR-001: Charging Station Communication Protocol

**Decision:** OCPP 1.6J over WebSocket with protocol abstraction layer.

**Alternatives Considered:**
- OCPP 2.0.1 from day one (rejected: fewer vendor implementations, more complex)
- MQTT + custom payload (rejected: no standard, vendor lock-in)
- HTTP polling (rejected: cannot meet 5s real-time requirement)

**Consequences:**
- Device Gateway module handles all OCPP translation
- Domain modules consume normalized events (`ChargingStarted`, `MeterValueReceived`, `ChargingStopped`)
- Migration to OCPP 2.0.1 only touches Device Gateway

---

### ADR-002: Architecture Style - Modular Monolith

**Decision:** Modular monolith with DDD-aligned modules and hexagonal architecture.

**Module Boundaries:**
| Module | Responsibility |
|--------|----------------|
| Identity & Access | AuthN/AuthZ, RBAC, user profiles |
| Station Management | Station registry, connectors, health, firmware metadata |
| Session Management | Charging session lifecycle, metering, state machine |
| Pricing & Billing | Tariff rules, cost calculation, invoicing |
| Payment Processing | Payment orchestration, provider integration |
| Notification | Multi-channel delivery, templates, preferences |
| Device Gateway | OCPP protocol handling, message routing, device auth |

**Principles:**
- Each module owns its data (schema)
- Modules communicate via domain events (in-process event bus)
- Ports define capabilities; adapters implement them (in-process now, REST/gRPC later)
- No shared database tables; no cross-schema queries

**Alternatives Considered:**
- Microservices from day one (rejected: operational overhead, distributed complexity, slower delivery)
- Fewer large modules (rejected: tighter coupling, bigger blast radius)
- More granular modules (rejected: over-engineering for current scale)

---

### ADR-003: Data Architecture

**Decision:** Single PostgreSQL instance with schema-per-module.

```
PostgreSQL
├── identity (schema)
├── station (schema)
├── session (schema)
├── billing (schema)
├── payment (schema)
├── notification (schema)
├── device_gateway (schema)
└── shared (reference data only - users, roles)
```

**Rules:**
- Module code only accesses its own schema (enforced by DB permissions / ArchUnit)
- Cross-module queries forbidden; use domain events + eventual consistency
- Reference data (station_id in session) = foreign key to other schema, read-only from owning module
- Flyway migrations per module (`db/migration/session/V1__...`)

**Alternatives Considered:**
- Separate DB instances per module (rejected: operational overhead for v1)
- Shared public schema with table prefixes (rejected: no enforcement, coupling)

---

### ADR-004: Inter-Module Communication

**Decision:** In-process domain events with lightweight orchestrator.

**Pattern:**
```
SessionMgmt.createSession() → SessionCreatedEvent
    → Pricing.reserveQuote() → QuoteReservedEvent
    → Payment.authorizeHold() → HoldAuthorizedEvent
    → DeviceGateway.sendStartTransaction() → StartSentEvent

ON FAILURE: Compensating events (PaymentHoldReleased, QuoteReleased, SessionMarkedFailed)
```

**Key Principles:**
- Saga orchestrator lives in Session module (owns lifecycle)
- Each step = async command via event bus
- Each module commits its own transaction
- No `@Transactional` spanning modules

**Alternatives Considered:**
- Direct service calls (rejected: temporal coupling, testing difficulty, extraction blocking)
- Full Temporal/Saga framework (rejected: overkill for v1; evolve when needed)

---

### ADR-005: External API Contract

**Decision:** REST + OpenAPI 3.1 for synchronous, SSE for real-time.

| Client | Protocol | Real-time |
|--------|----------|-----------|
| Mobile App | REST | SSE (session progress, charger status) |
| Admin Dashboard | REST | SSE (station health, alerts) |
| Vendor Portal | REST | SSE (firmware status, telemetry) |
| Charging Stations | OCPP 1.6J (WebSocket) | Native bidirectional |

**API Versioning:** URL path (`/api/v1/...`)

**Alternatives Considered:**
- GraphQL (rejected: overkill, learning curve, caching complexity)
- gRPC (rejected: browser support complexity, grpc-web proxy needed)

---

### ADR-006: Authentication & Authorization

**Decision:** Spring Cloud Gateway + Spring Security OAuth2 Resource Server with abstract IdP.

**Token Flow:**
```
Mobile App / Dashboard / Vendor Portal
    → Keycloak / Azure AD / External IdP (OIDC)
    → JWT Access Token
    → Spring Cloud Gateway (JWT validation, token relay)
    → Module (local JWT validation via JWKS cache)
```

**Roles:** `ADMIN`, `VENDOR`, `CUSTOMER` (Keycloak realm roles)
**Scopes:** `station:read`, `station:write`, `session:start`, `billing:read`, etc.

**Alternatives Considered:**
- Embedded Keycloak (rejected: operational burden for MVP)
- Custom auth (rejected: spec compliance complexity, PCI scope)

---

### ADR-007: Device Gateway

**Decision:** Dedicated module, protocol boundary isolated from business domains.

**Responsibilities:**
- OCPP 1.6J WebSocket server (Spring WebFlux/Netty ready)
- Device authentication (X.509 mutual TLS + OCPP Authorize)
- Message translation: OCPP frames ↔ Domain events
- Session affinity (sticky WebSocket or Redis-backed registry)
- Horizontal scaling via Redis Pub/Sub for cross-instance routing

**Alternatives Considered:**
- Embedded in Station Management (rejected: couples domain to protocol)
- Blocking Spring MVC WebSocket (accepted for MVP; reactive path preserved)

---

### ADR-008: Billing & Payment Architecture

**Decision:** Separate modules; Billing owns policy, Payment owns provider integration.

**Pricing Model:** Pluggable `TariffStrategy` (energy, time, session fee, idle fee, dynamic)

**Money:** JSR 354 (`org.javamoney:moneta`) — never raw `BigDecimal`

**Payment Providers:** Adapter pattern (`StripeAdapter`, `AdyenAdapter`, `WorldlineAdapter`)

**PCI DSS:** Never touch PAN; use provider PaymentIntent/SetupIntent; store only `payment_method_id`

**Saga Flow:**
```
SessionEnded → CalculateCost → ReserveFunds (auth) → CapturePayment → GenerateInvoice
    ↓ (any failure)
Compensate: void auth, credit balance, alert
```

**Idempotency:** Every payment operation keyed by `session_id + action`

---

### ADR-009: Observability

**Decision:** OpenTelemetry instrumentation from day one; Grafana stack backend.

**Signals:**
- **Logs:** Structured JSON → Loki (with `trace_id`, `span_id`, `session_id`, `station_id`)
- **Metrics:** Micrometer → Mimir (RED + business: sessions_active, chargers_online, payment_success_rate)
- **Traces:** OTel Java Agent → Tempo (baggage: `session_id` propagated across OCPP, payment)
- **Alerts:** PromQL rules → Alertmanager → PagerDuty/Slack

**Key Alerts:**
- `charger_offline > 5m`
- `payment_failure_rate > 1%`
- `session_start_latency_p99 > 2s`

---

### ADR-010: Deployment & Infrastructure

**Decision:** Single-region multi-AZ, containerized, Helm deployment.

**Target Platform:** Managed Kubernetes (EKS/GKE/AKS) or managed containers (Cloud Run/App Runner) for MVP

**Infrastructure:**
- PostgreSQL: Managed (Cloud SQL / RDS / Azure Postgres)
- Redis: Managed (ElastiCache / Memorystore)
- Object Storage: S3/GCS (firmware artifacts, invoices)
- Ingress: Nginx + cert-manager (Let's Encrypt) or cloud LB

**Environments:** `dev` (per-PR), `staging` (prod mirror), `prod`

**GitOps Path:** ArgoCD + Kustomize/Helm overlays when team/scale justifies

---

### ADR-011: CI/CD Pipeline

**Decision:** Build → Test → Scan → Helm deploy; GitOps-ready.

**Pipeline Stages:**
1. PR → Build → Unit Tests → Integration Tests → Contract Tests (OpenAPI)
2. Build Docker image (multi-stage, distroless)
3. Security scan (Trivy, Grype) + SBOM (Syft)
4. Sign image (cosign)
5. Push to registry (GHCR/ECR)
6. Helm deploy to target environment
7. Rolling update with health checks + rollback capability

**Secrets:** External Secrets Operator + AWS Secrets Manager / Vault

---

### ADR-012: Multi-Tenancy (Vendor Isolation)

**Decision:** Single platform, vendor-scoped data with Row-Level Security ready.

**Model:**
```sql
ALTER TABLE stations ENABLE ROW LEVEL SECURITY;
CREATE POLICY vendor_isolation ON stations
  USING (vendor_id = current_setting('app.current_vendor_id')::uuid);
```

**Vendor Portal:** Filtered views via JWT `vendor_id` claim
**Admin Portal:** Global view
**White-label Path:** Separate deployment per tenant (not schema isolation)

---

### ADR-013: Charging Session State Machine

**Decision:** Simplified v1; clear ownership split.

| Layer | Responsibility |
|-------|----------------|
| Device Gateway | OCPP protocol state (StartTransaction.conf, StopTransaction.req, MeterValues) |
| Session Management | Business session lifecycle (PENDING → CHARGING → COMPLETED → INVOICED) |

**Events Published by Gateway:** `ChargingStarted`, `MeterValueReceived`, `ChargingStopped`
**Session Mgmt:** Consumes events, controls transitions, publishes `SessionCompleted` → Billing/Payment

**Edge Cases Deferred to v2:**
- Full reconciliation jobs
- Sophisticated timeout guards
- Clock drift handling

---

## Consequences

### Positive
- Fast delivery with clean boundaries
- No extraction-blocking technical debt
- Operational simplicity for MVP
- Clear evolution path for every decision
- PCI DSS scope minimized
- Vendor-neutral observability

### Negative / Risks
- In-process events = single process failure domain (mitigated: modular monolith can run multiple replicas)
- Single-region = regional outage risk (mitigated: multi-AZ, warm standby plan)
- Simplified state machine = edge cases handled manually initially (mitigated: clear ownership for future enhancement)
- Contract testing deferred = breaking changes caught later (mitigated: OpenAPI validation, module integration tests)

---

## Validation Criteria

- [ ] Modular monolith builds and deploys successfully
- [ ] OCPP 1.6J charger connects, starts/stops session
- [ ] Session → Billing → Payment flow works end-to-end
- [ ] OpenAPI spec validates against implementation
- [ ] OTel traces propagate `session_id` across modules
- [ ] Helm chart deploys to Kubernetes with rolling update
- [ ] Backup/restore tested quarterly

---

## Related Decisions

- ADR-001 → ADR-007 (Protocol → Device Gateway)
- ADR-002 → ADR-003 (Architecture → Data)
- ADR-004 → ADR-008 (Communication → Billing/Payment Saga)
- ADR-006 → ADR-012 (Auth → Multi-Tenancy)

---

## Revision History

| Date | Author | Changes |
|------|--------|---------|
| 2026-07-24 | Architecture Team | Initial version from grilling session |

---