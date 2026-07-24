# System Architecture

## Overview
Modular monolith with DDD-aligned modules and hexagonal (ports & adapters) architecture per module. Enforces clean dependency direction: API → Application → Domain ← Infrastructure. Spring Modulith validates module boundaries at build time.

## Architecture Style

**Modular Monolith** (ADR-0001 Decision #2)

**Structure**:
```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ Identity │  │ Station  │  │ Session  │  │ Billing  │  ...   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘        │
│       │             │             │             │               │
├───────┼─────────────┼─────────────┼─────────────┼───────────────┤
│       ▼             ▼             ▼             ▼               │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    Domain Layer                          │    │
│  │  Aggregates │ Entities │ Value Objects │ Domain Events  │    │
│  │  Repositories (Ports) │ Domain Services                 │    │
│  └─────────────────────────────────────────────────────────┘    │
│       ▲             ▲             ▲             ▲               │
│       │             │             │             │               │
│  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐        │
│  │Identity  │  │ Station  │  │ Session  │  │ Billing  │        │
│  │ Infra    │  │ Infra    │  │ Infra    │  │ Infra    │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

**Module Boundaries** (ADR-0001 Decision #2):
| Module | Responsibility | Key Aggregates |
|--------|----------------|----------------|
| Identity & Access | AuthN/AuthZ, RBAC, user profiles | User, Role, Permission |
| Station Management | Station registry, connectors, health, firmware | Station, Connector, Firmware |
| Session Management | Charging session lifecycle, metering, state machine | ChargingSession, MeterReading |
| Pricing & Billing | Tariff rules, cost calculation, invoicing | Tariff, Invoice, BillingAccount |
| Payment Processing | Payment orchestration, provider integration | Payment, PaymentMethod |
| Notification | Multi-channel delivery, templates, preferences | Notification, Template, Channel |
| Device Gateway | OCPP protocol handling, message routing, device auth | DeviceConnection, OcppMessage |
| **Vehicle Management** | **Vehicle lifecycle, RFID, ownership transfer, de-registration** | **Vehicle, OwnershipTransfer** |

**Dependency Rules** (Enforced by Spring Modulith + ArchUnit):

```
ALLOWED:
  API → Application → Domain
  Infrastructure → Application Ports (implements)
  Infrastructure → Domain (read-only, via ports)

FORBIDDEN:
  Domain → Spring Framework (any)
  Domain → JPA/Hibernate
  Domain → REST APIs / HTTP clients
  Domain → Kafka / Message brokers
  Domain → Redis / Caches
  Domain → External SDKs
  Application → Infrastructure (concrete)
  Module A Domain → Module B Domain (use events)
```

---

## Module Communication

> **Core Rule**: Each business module remains autonomous. Collaboration happens ONLY through well-defined interfaces. Modules must NEVER bypass encapsulation by accessing another module's internal classes.

### Communication Methods

| Preferred ✅ | Avoid ❌ |
|--------------|----------|
| Application Services (input ports) | Direct repository access |
| Published Interfaces (ports) | Direct entity manipulation |
| Domain Events | Cross-module transactions |
| Message/Event Bus | Shared database tables |

### Examples

**Good — Application Service Call**:
```
Session Module          Billing Module
    │                        │
    ▼                        │
BillingApplicationService   │  (implements BillingPort interface)
    │                        │
    └────────────────────────┘
```

**Better — Domain Event (Async)**:
```
SessionCompletedEvent
         │
         ▼
    Event Bus
         │
    ┌────┴────┐
    ▼         ▼
Billing   Payment
Module    Module
(consume) (consume)
```

**Bad — Direct Repository Access**:
```
SessionService
      │
      ▼
BillingRepository  ❌ FORBIDDEN — breaks module encapsulation
```

### Implementation Rules

1. **Exported API Package**: Each module defines `api/` package with:
   - Input ports (Application Service interfaces)
   - Output ports (Repository interfaces for infrastructure)
   - API DTOs (Request/Response)
   - Domain Events (published)

2. **Spring Modulith Verification**:
   ```java
   @Test
   void verifyModuleStructure() {
       Modules modules = Modules.of(Application.class);
       modules.verify();  // Fails on forbidden dependencies
   }
   ```

3. **ArchUnit Rules** (in `archunit/` test):
   ```java
   @ArchTest
   static final ArchRule noCrossModuleRepositoryAccess =
       noClasses()
           .that().resideInAPackage("..session..")
           .should().accessClassesThat().resideInAPackage("..billing..repository..");
   ```

4. **Saga Orchestration** (Session module owns lifecycle):
   ```java
   @Service
   @RequiredArgsConstructor
   public class SessionSagaOrchestrator {
       private final ApplicationEventPublisher events;
       
       public void handleSessionEnded(SessionEndedEvent event) {
           // Step 1: Calculate cost
           events.publishEvent(new CalculateCostCommand(event.sessionId()));
           
           // Step 2: On QuoteReservedEvent → ReserveFundsCommand
           // Step 3: On HoldAuthorizedEvent → StartTransactionCommand
           // Compensation on any failure
       }
   }
   ```

5. **Event Naming Convention**:
   - Past tense: `SessionCreatedEvent`, `PaymentCapturedEvent`, `StationRegisteredEvent`
   - Contain only immutable data (IDs, values, timestamps)
   - No entity references — only identifiers

6. **Event Publishing** (in domain aggregate):
   ```java
   @AggregateRoot
   public class ChargingSession {
       @DomainEvents
       Collection<Object> domainEvents() {
           return List.of(new SessionStartedEvent(this.id, this.stationId, ...));
       }
   }
   ```

### Decision References

- ADR-0001 Decision #4: Inter-Module Communication
- ADR-0001 Decision #8: Billing & Payment Saga
- ADR-0001 Decision #13: Charging Session State Machine

## API Design

**Frontend**: React Query (TanStack Query) for server state + Zustand for client state
- Server state: Cached, deduplicated, background refetch
- Client state: UI preferences, auth tokens, ephemeral filters
- **No Redux** — avoid global store for server data

**Backend**: 
- **Session state**: In-memory (request-scoped) + Database (persistent)
- **Caching**: Redis for reference data, rate limiting, refresh tokens
- **No distributed cache** for domain data — module owns its data

## Caching Strategy

| Layer | Technology | Use Case | TTL |
|-------|------------|----------|-----|
| HTTP | CDN / Nginx | Static assets, OpenAPI spec | 1 year / 1 hour |
| API | Spring Cache + Redis | Reference data (tariffs, stations) | 5-15 min |
| Domain | Caffeine (in-process) | Hot lookups (connector types) | 1 hour |
| Auth | Redis | Refresh tokens, rate limits | Token TTL |
| Session | Redis | Device gateway session affinity | Session TTL |

**Cache Invalidation**: Event-driven via domain events (`TariffUpdatedEvent` → evict tariff cache)

## Security Patterns

**Authentication** (ADR-0001 Decision #6):
- Spring Cloud Gateway: JWT validation, token relay to modules
- Modules: Local JWT validation via JWKS cache (no network call per request)
- IdP: Keycloak / Azure AD / External OIDC (abstracted)

**Authorization**:
- Role-based: `ADMIN`, `VENDOR`, `CUSTOMER` (Keycloak realm roles)
- Scope-based: `station:read`, `station:write`, `session:start`, `billing:read`
- Module-level: `@PreAuthorize` on application services

**Data Protection**:
- TLS 1.3 everywhere (ingress + inter-service)
- mTLS for Device Gateway ↔ Chargers (X.509)
- PCI DSS: Never store PAN; tokenize via payment provider
- Encryption at rest: Managed PostgreSQL + Redis encryption
- Field-level: JSR 354 Moneta for money, pgcrypto for PII

**Rate Limiting** (ADR-0001 Decision #11):
- Gateway: Global + per-IP + per-user
- Device Gateway: Per-station OCPP message rate
- Domain: Business-rate limits (e.g., max concurrent sessions per station)

## Observability (ADR-0001 Decision #9)

**OpenTelemetry** auto-instrumentation (Java agent) + manual spans for business operations

**Signals**:
- **Logs**: Structured JSON → Loki (trace_id, span_id, session_id, station_id)
- **Metrics**: Micrometer → Mimir (RED + business: `sessions_active`, `chargers_online`, `payment_success_rate`)
- **Traces**: OTel → Tempo (baggage: `session_id` propagated across OCPP, payment)
- **Alerts**: PromQL → Alertmanager → PagerDuty/Slack

**Key Dashboards**:
- Charging Session Funnel (start → charging → complete → invoiced)
- Station Health (online, charging, error, maintenance)
- Payment Success Rate by Provider
- API Latency (p50, p95, p99) by endpoint

## Multi-Tenancy (ADR-0001 Decision #12)

**Model**: Single platform, vendor-scoped data with Row-Level Security (RLS)

```sql
ALTER TABLE stations ENABLE ROW LEVEL SECURITY;
CREATE POLICY vendor_isolation ON stations
  USING (vendor_id = current_setting('app.current_vendor_id')::uuid);
```

**Access**:
- Vendor Portal: Filtered views via JWT `vendor_id` claim
- Admin Portal: Global view (bypass RLS via `SET LOCAL app.current_vendor_id = '0000...'`)
- White-label Path: Separate deployment per tenant (not schema isolation)

## Deployment Architecture (ADR-0001 Decision #10)

**Target**: Single-region multi-AZ Kubernetes (EKS/GKE/AKS)

**Components**:
```
┌─────────────────────────────────────────────────────────────┐
│                      Kubernetes Cluster                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   Ingress    │  │  Modular     │  │  Device      │       │
│  │  (Nginx)     │──▶│  Monolith    │──▶│  Gateway     │       │
│  │  TLS Term.   │  │  (HPA)       │  │  (WebSocket) │       │
│  └──────────────┘  └──────┬───────┘  └──────────────┘       │
│                           │                                    │
│         ┌─────────────────┼─────────────────┐                 │
│         ▼                 ▼                 ▼                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ PostgreSQL   │  │    Redis     │  │  Object      │       │
│  │ (Managed)    │  │ (Managed)    │  │  Storage     │       │
│  │ Multi-AZ     │  │ Multi-AZ     │  │  (S3/GCS)    │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

**Environments**: `dev` (per-PR), `staging` (prod mirror), `prod`

**GitOps**: ArgoCD + Helm overlays (when team/scale justifies)

## Decision Relationships

- **Tech Stack → Architecture**: Spring Boot 4 + Spring Modulith enables modular monolith
- **Data Stack → Architecture**: Schema-per-module enforces module boundaries at DB level
- **Architecture → API Design**: Module APIs exposed via REST; internal via events
- **Architecture → Security**: Module boundaries = authorization boundaries
- **Architecture → Observability**: Module + correlation IDs for distributed tracing
- **Architecture → Deployment**: Single deployable unit; modules scale together (MVP)