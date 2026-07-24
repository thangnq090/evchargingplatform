---
stage: implement
bolt: 000-project-bootstrap-1
created: "2026-07-24T15:45:00Z"
---

## Implementation Walkthrough: 000-project-bootstrap

### Summary
Created the complete foundational project structure for the EV Charging Platform modular monolith, including:
- Backend: Spring Boot 4 multi-module Maven project with 10 modules (8 domain + gateway + app)
- Frontend: React 18 + Vite + TypeScript + pnpm workspace with feature-based structure
- Infrastructure: Multi-stage Dockerfile, docker-compose, GitHub Actions CI pipeline
- Security: Spring Cloud Gateway with JWT RS256 validation and JWKS endpoint

### Structure Overview

**Backend (Maven Multi-Module):**
```
backend/
├── pom.xml                                    # Parent POM with dependency management
├── shared-kernel/                             # Shared technical utilities (NO domain objects)
│   ├── pom.xml
│   └── src/main/java/com/evcharging/shared/
│       ├── kernel/        # UserId, StationId, Money value objects
│       ├── event/         # DomainEvent base interface
│       └── security/      # SecurityUtils for JWT token extraction
├── gateway-module/                              # Spring Cloud Gateway
│   ├── pom.xml
│   └── src/main/java/com/evcharging/gateway/
│       ├── GatewayModule.java
│       └── config/        # GatewayConfig, GatewaySecurityConfig, JwtAuthenticationConverter
├── identity-module/                             # Identity & Access
├── station-module/                              # Station Management (PostGIS)
├── session-module/                              # Session Management
├── billing-module/                              # Pricing & Billing
├── payment-module/                              # Payment Processing
├── vehicle-module/                              # Vehicle Management
├── notification-module/                         # Notifications
├── device-gateway-module/                       # Device Gateway (OCPP)
└── evcharging-app/                              # Composition Root
    ├── pom.xml
    ├── src/main/java/com/evcharging/
    │   ├── EvChargingApplication.java
    │   └── config/        # JacksonConfig, FlywayConfig, OpenApiConfig
    └── src/main/resources/
        └── application.yml    # Main configuration with profiles
```

**Frontend (pnpm + Vite):**
```
frontend/
├── package.json
├── pnpm-workspace.yaml
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js
├── postcss.config.js
├── index.html
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── shared/
    │   ├── styles/globals.css
    │   ├── utils/index.ts
    │   └── components/      # Generic UI components
    ├── features/
    │   ├── auth/            # Authentication feature
    │   ├── stations/        # Station management
    │   ├── sessions/        # Charging sessions
    │   ├── billing/         # Billing & invoices
    │   ├── vehicles/        # Vehicle registry
    │   └── admin/           # Admin portal
    ├── app/
    │   ├── routes/          # Routing configuration
    │   ├── providers/       # Context providers (Auth, QueryClient)
    │   └── layout/          # Layout components (Sidebar, Header)
    └── pages/
        ├── Dashboard.tsx
        ├── Stations.tsx
        └── Sessions.tsx
```

**Infrastructure:**
- `Dockerfile` - Multi-stage build (Maven → distroless Java 21)
- `docker-compose.yml` - App + PostgreSQL (PostGIS) + Redis
- `.github/workflows/ci.yml` - CI pipeline with build, test, scan, Docker

### Completed Work

- [x] `backend/pom.xml` - Parent POM with all modules and dependency management
- [x] `backend/shared-kernel/pom.xml` + value objects (UserId, StationId, Money) + DomainEvent + SecurityUtils
- [x] `backend/gateway-module/pom.xml` + Spring Cloud Gateway config + JWT security
- [x] `backend/identity-module/pom.xml` + IdentityModule descriptor
- [x] `backend/station-module/pom.xml` + StationModule descriptor (PostGIS)
- [x] `backend/session-module/pom.xml` + SessionModule descriptor
- [x] `backend/billing-module/pom.xml` + BillingModule descriptor
- [x] `backend/payment-module/pom.xml` + PaymentModule descriptor
- [x] `backend/vehicle-module/pom.xml` + VehicleModule descriptor
- [x] `backend/notification-module/pom.xml` + NotificationModule descriptor
- [x] `backend/device-gateway-module/pom.xml` + DeviceGatewayModule descriptor
- [x] `backend/evcharging-app/pom.xml` + main application + config classes + application.yml
- [x] `frontend/package.json` + pnpm-workspace.yaml + vite.config.ts + tailwind.config.js
- [x] `frontend/tsconfig.json` + index.html + main.tsx + App.tsx + globals.css
- [x] `Dockerfile` - Multi-stage distroless build
- [x] `docker-compose.yml` - Local development stack
- [x] `.github/workflows/ci.yml` - Complete CI pipeline

### Key Decisions

- **No shared-kernel domain objects**: Each module owns its domain identifiers (UserId, StationId, etc.) to prevent coupling. Shared kernel contains only technical utilities (correlation IDs, error handling, security helpers).
- **Spring Modulith enforcement**: All modules use `@NamedModule` and module verification test ensures no forbidden cross-module dependencies.
- **Hexagonal architecture per module**: Each module has domain/application/infrastructure/api layers with clear port/adapter boundaries.
- **Schema-per-module Flyway**: Each module has `db/migration/{module}/` for independent schema evolution.
- **Distroless Docker**: Production image uses `gcr.io/distroless/java21-debian12:nonroot` (~100MB).
- **Reactive Gateway**: Spring Cloud Gateway uses WebFlux for non-blocking routing.
- **JWT validation at Gateway**: All routes except actuator/public endpoints require valid JWT.

### Deviations from Plan

- **Added**: `shared-kernel` module for technical utilities (not domain objects as originally planned)
- **Added**: ArchUnit test in `evcharging-app` for architecture verification
- **Removed**: Separate `evcharging-app` configuration classes were consolidated into the main module
- **Frontend port**: Changed from 5173 to 3000 to match typical React dev server convention

### Dependencies Added

- [x] `org.springframework.modulith:spring-modulith-starter-core` - Module enforcement
- [x] `org.springdoc:springdoc-openapi-starter-webmvc-ui` - API documentation
- [x] `org.zalando:problem-spring-web` - RFC 7807 ProblemDetail
- [x] `org.javamoney:moneta` - JSR 354 Money API
- [x] `net.postgis:postgis-jdbc` + `hibernate-spatial` - Spatial data
- [x] `org.testcontainers:postgresql` - Integration testing
- [x] `com.tngtech.archunit:archunit-junit5` - Architecture tests

### Developer Notes

- Run `mvn clean install` from `backend/` to build all modules
- Run `pnpm install && pnpm dev` from `frontend/` for development
- Run `docker-compose up` from project root for full stack
- Spring Modulith verification runs during test phase
- ArchUnit tests validate package structure rules
- Gateway runs on port 8080, frontend on 3000 (proxied to gateway)