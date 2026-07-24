---
last_updated: "2026-07-24T14:45:00Z"
total_decisions: 17
---

# Decision Index

This index tracks all Architecture Decision Records (ADRs) created during Construction bolts.
Use this to find relevant prior decisions when working on related features.

## How to Use

**For Agents**: Scan the "Read when" fields below to identify decisions relevant to your current task. Before implementing new features, check if existing ADRs constrain or guide your approach. Load the full ADR for matching entries.

**For Humans**: Browse decisions chronologically or search for keywords. Each entry links to the full ADR with complete context, alternatives considered, and consequences.

---

## Decisions

<!-- Entries are appended below in reverse chronological order (newest first) -->

### ADR-017: Multi-Tenancy (Vendor Isolation)
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-012`
- **Summary**: Single platform with vendor-scoped data using Row-Level Security. Vendor Portal filtered via JWT vendor_id claim; Admin bypasses RLS. White-label via separate deployments.
- **Read when**: Implementing vendor isolation, data access control, multi-tenant queries, vendor portal features, admin global views

### ADR-016: Deployment & Infrastructure
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-010`
- **Summary**: Single-region multi-AZ Kubernetes (EKS/GKE/AKS) with managed PostgreSQL, Redis, Object Storage. Helm deployment, rolling updates. GitOps path preserved for future.
- **Read when**: Designing deployment pipelines, infrastructure provisioning, scaling strategies, HA configuration, environment promotion

### ADR-015: CI/CD Pipeline
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-011`
- **Summary**: Build → Test → Scan → Helm deploy via GitHub Actions. Multi-arch Docker images, Trivy/Grype scanning, SBOM generation, cosign signing, ArgoCD/Flux GitOps ready.
- **Read when**: Setting up CI/CD, container builds, security scanning, release automation, GitOps adoption

### ADR-014: Disaster Recovery
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-013`
- **Summary**: Managed PostgreSQL backups + immutable audit log, tested quarterly restores. RPO/RTO defined for transactional data.
- **Read when**: Implementing backup strategies, audit logging, compliance requirements, recovery testing

### ADR-013: Testing Strategy
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-012`
- **Summary**: Unit + module integration + OpenAPI validation + OCPP simulator. Contract testing deferred to extraction phase. Fast feedback prioritized.
- **Read when**: Writing tests, setting up test infrastructure, test pyramid decisions, OCPP simulation, contract testing

### ADR-012: Rate Limiting
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-011`
- **Summary**: Gateway + Device Gateway basics with extension points. MVP protection; domain-specific limits added as patterns emerge.
- **Read when**: Implementing API rate limiting, OCPP message throttling, abuse prevention, quota management

### ADR-011: Observability Stack
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-009`
- **Summary**: OpenTelemetry from day one → Grafana stack (Loki, Mimir, Tempo). Structured JSON logs with trace_id, span_id, session_id, station_id. RED + business metrics. Key alerts: charger offline >5m, payment failure >1%, session start p99 >2s.
- **Read when**: Adding instrumentation, creating dashboards, defining alerts, log formatting, trace propagation, metrics design

### ADR-010: Charging Session State Machine
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-013`
- **Summary**: Simplified v1 with clear ownership split: Device Gateway owns OCPP protocol state; Session Management owns business lifecycle (PENDING → CHARGING → COMPLETED → INVOICED). Edge cases deferred to v2.
- **Read when**: Implementing session lifecycle, OCPP message handling, state transitions, event publishing, reconciliation jobs

### ADR-009: Billing & Payment Architecture
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-008`
- **Summary**: Separate Billing (policy) and Payment (provider) modules. Pluggable TariffStrategy. JSR 354 Moneta for money. Provider adapters (Stripe/Adyen/Worldline). Saga with compensation: SessionEnded → CalculateCost → ReserveFunds → CapturePayment → GenerateInvoice. Idempotency via session_id + action.
- **Read when**: Implementing pricing, invoicing, payment flows, provider integrations, compensation logic, idempotency keys

### ADR-008: Device Gateway
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-007`
- **Summary**: Dedicated module, protocol boundary isolated. OCPP 1.6J WebSocket (Spring WebFlux/Netty ready). X.509 mTLS + OCPP Authorize. Message translation: OCPP frames ↔ Domain events. Session affinity via sticky WS or Redis registry. Horizontal scaling via Redis Pub/Sub.
- **Read when**: Implementing OCPP server, device authentication, protocol translation, WebSocket scaling, charger connectivity

### ADR-007: Authentication & Authorization
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-006`
- **Summary**: Spring Cloud Gateway + Spring Security OAuth2 Resource Server with abstract IdP. JWT flow: Client → IdP (Keycloak/Azure AD) → JWT → Gateway (validate, relay) → Modules (local JWKS cache). Roles: ADMIN, VENDOR, CUSTOMER. Scopes: station:read, station:write, session:start, billing:read.
- **Read when**: Implementing auth, token validation, role/scope checks, IdP integration, gateway configuration, module security

### ADR-006: External API Contract
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-005`
- **Summary**: REST + OpenAPI 3.1 for sync, SSE for real-time. URL path versioning (/api/v1/). Clients: Mobile (REST+SSE), Admin (REST+SSE), Vendor Portal (REST+SSE), Chargers (OCPP 1.6J WS). GraphQL/gRPC rejected.
- **Read when**: Designing REST endpoints, OpenAPI spec, SSE streams, API versioning, client integration

### ADR-005: Inter-Module Communication
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-004`
- **Summary**: In-process domain events + lightweight saga orchestrator (Session module). Pattern: SessionCreated → QuoteReserved → HoldAuthorized → StartSent. On failure: compensating events (HoldReleased, QuoteReleased, SessionFailed). No @Transactional across modules.
- **Read when**: Implementing cross-module flows, saga orchestration, event publishing/consuming, transaction boundaries, compensation logic

### ADR-004: Data Architecture
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-003`
- **Summary**: Single PostgreSQL, schema-per-module. Modules: identity, station, session, billing, payment, notification, device_gateway, shared. Rules: module owns schema, no cross-schema joins, FKs read-only from other modules, Flyway per module.
- **Read when**: Designing database schema, writing migrations, querying across modules, enforcing module boundaries, Flyway configuration

### ADR-003: Architecture Style - Modular Monolith
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-002`
- **Summary**: Modular monolith with DDD modules + hexagonal ports/adapters. Modules: Identity, Station, Session, Billing, Payment, Notification, Device Gateway. Principles: module owns data, events for communication, ports define capabilities, no shared tables. Spring Modulith for enforcement.
- **Read when**: Creating new modules, defining module boundaries, implementing ports/adapters, enforcing modularity, Spring Modulith usage

### ADR-002: Charging Station Communication Protocol
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md#adr-001`
- **Summary**: OCPP 1.6J over WebSocket with protocol abstraction layer. Industry standard, meets 5s real-time requirement. Abstraction enables OCPP 2.0.1 migration. Device Gateway handles translation to domain events (ChargingStarted, MeterValueReceived, ChargingStopped).
- **Read when**: Implementing charger communication, OCPP message handling, protocol abstraction, WebSocket server, domain event design

### ADR-001: Project Foundation
- **Status**: accepted
- **Date**: 2026-07-24
- **Bolt**: N/A (Pre-initialization)
- **Path**: `docs/architecture/ADR-0001-architecture-decisions.md`
- **Summary**: EV Charging Platform - cloud-based platform for managing EV charging stations. Constraints: aggressive timeline, thousands of stations, millions sessions/year, 99.9% availability, PCI DSS. Core decisions documented in ADR-002 through ADR-017.
- **Read when**: Understanding overall project context, onboarding, high-level architecture review

---

## Search Index (Keywords → ADRs)

| Keyword | Relevant ADRs |
|---------|---------------|
| modular monolith | ADR-003 |
| hexagonal | ADR-003 |
| spring modulith | ADR-003 |
| ddd | ADR-003, ADR-004 |
| module boundaries | ADR-003, ADR-004 |
| database | ADR-004 |
| schema-per-module | ADR-004 |
| flyway | ADR-004 |
| domain events | ADR-005 |
| saga | ADR-005, ADR-009 |
| compensation | ADR-005, ADR-009 |
| rest | ADR-006 |
| openapi | ADR-006 |
| sse | ADR-006 |
| api versioning | ADR-006 |
| authentication | ADR-007 |
| jwt | ADR-007 |
| oauth2 | ADR-007 |
| keycloak | ADR-007 |
| rbac | ADR-007 |
| scopes | ADR-007 |
| ocpp | ADR-002, ADR-008 |
| websocket | ADR-002, ADR-008 |
| device gateway | ADR-008 |
| protocol abstraction | ADR-002 |
| billing | ADR-009 |
| payment | ADR-009 |
| tariff | ADR-009 |
| moneta | ADR-009 |
| idempotency | ADR-009 |
| observability | ADR-011 |
| opentelemetry | ADR-011 |
| grafana | ADR-011 |
| loki | ADR-011 |
| tempo | ADR-011 |
| mimir | ADR-011 |
| alerts | ADR-011 |
| session state machine | ADR-010 |
| deployment | ADR-016 |
| kubernetes | ADR-016 |
| helm | ADR-016 |
| ci/cd | ADR-015 |
| github actions | ADR-015 |
| disaster recovery | ADR-014 |
| backup | ADR-014 |
| testing | ADR-013 |
| ocpp simulator | ADR-013 |
| rate limiting | ADR-012 |
| multi-tenancy | ADR-017 |
| row level security | ADR-017 |
| vendor isolation | ADR-017 |