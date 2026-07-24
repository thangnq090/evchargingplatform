---
unit: 000-project-bootstrap
intent: 001-ev-charging-mvp
phase: inception
status: draft
created: "2026-07-24T15:00:00Z"
updated: "2026-07-24T15:00:00Z"
---

# Unit Brief: Project Bootstrap & Scaffolding

## Purpose
Create the foundational project structure for the modular monolith backend and frontend application based on the approved architecture decisions. This unit sets up folder conventions, module boundaries, dependency management, build configuration, and CI/CD pipeline — enabling all other units to work within a consistent structure.

## Scope

### In Scope
- Spring Boot 4 backend project with Maven multi-module structure
- React 18 + Vite + TypeScript frontend project with pnpm workspaces
- Modular monolith module boundaries (8 modules) with Spring Modulith
- Hexagonal architecture package conventions per module (domain, application, infrastructure, api)
- Spring Cloud Gateway setup for API routing and JWT validation
- PostgreSQL schema-per-module with Flyway migration directory structure
- PostGIS dependency and spatial configuration
- Docker image build (multi-stage) + docker-compose for local development
- OpenAPI / Swagger UI configuration (springdoc-openapi)
- Maven wrapper, pnpm workspace config, ESLint, Prettier
- CI pipeline (GitHub Actions): build → test → scan → containerize
- Base entity classes, value objects, shared kernel (UserId, StationId, Money)
- README with local development setup instructions
- JWT RS256 key pair generation script

### Out of Scope
- Any business logic implementation (handled by other units)
- Domain model design (bolt-specific)
- Testing beyond scaffolding validation
- Deployment infrastructure (Helm charts, K8s manifests)

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| N/A | Project structure setup — foundational prerequisite | Must |

---

## Domain Concepts

### Key Operations
| Operation | Description | Inputs | Outputs |
|-----------|-------------|--------|---------|
| Create Backend Structure | Maven multi-module project with module boundaries | Project config | `pom.xml`, module dirs |
| Create Frontend Structure | React+Vite TypeScript project | Project config | `package.json`, Vite config |
| Configure Spring Modulith | Module verification tests, package structure | Module list | Module descriptors |
| Configure Spring Cloud Gateway | Gateway module, JWT validation filter chain | JWT config | Gateway config |
| Setup PostgreSQL Schema | Schema-per-module + Flyway migration dirs | Schema list | SQL migration dirs |
| Configure CI Pipeline | GitHub Actions workflow | Build config | `.github/workflows/` |
| Generate Shared Kernel | Base entities, value objects, common utilities | - | Shared library |
| Setup Docker Build | Multi-stage Dockerfile + docker-compose | Build config | `Dockerfile`, `docker-compose.yml` |

---

## Story Summary

| Metric | Count |
|--------|-------|
| Total Stories | ~4 |
| Must Have | 3 |
| Should Have | 1 |
| Could Have | 0 |

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| None | This is the foundational bootstrap unit |

### Depended By
| Unit | Reason |
|------|--------|
| All other units | Require project structure to work within |

### External Dependencies
| System | Purpose | Risk |
|--------|---------|------|
| Maven / Java 21 | Build tooling | Low |
| pnpm / Node.js | Frontend tooling | Low |
| Spring Initializr | Starting point for Spring Boot project | Low — only for initial generation |

---

## Technical Context

### Suggested Technology
| Component | Technology |
|-----------|------------|
| Backend Build | Maven (multi-module), Java 21 |
| Frontend Build | pnpm workspaces, Vite 6, TypeScript |
| Monolith Structure | Spring Modulith with `@NamedModule` |
| API Gateway | Spring Cloud Gateway (in-process for modular monolith) |
| API Docs | springdoc-openapi + Swagger UI |
| Database | PostgreSQL 16 + Flyway + PostGIS |
| Container | Docker multi-stage (distroless base) |
| CI/CD | GitHub Actions |
| JWT Generation | OpenSSL script for RS256 key pair |

### Project Structure (Backend + Frontend)
```
ev-charging-platform/
│
├── backend/
│   ├── pom.xml (parent + aggregator)
│   │
│   ├── evcharging/                     ← Main application entry point & composition root
│   │   ├── pom.xml
│   │   └── src/main/java/com/evcharging/
│   │       ├── EvChargingApplication.java    (@SpringBootApplication)
│   │       ├── EvChargingModule.java         (@NamedModule — Spring Modulith)
│   │       ├── config/
│   │       │   ├── JacksonConfig.java        (ObjectMapper, JSR 354 Money)
│   │       │   ├── FlywayConfig.java         (Multi-schema Flyway migrations)
│   │       │   └── OpenApiConfig.java        (springdoc, Swagger UI)
│   │       └── application.properties / .yaml (main config)
│   │
│   ├── shared-kernel/
│   │   └── src/main/java/com/evcharging/shared/
│   │       ├── kernel/                       (Value Objects: UserId, StationId, Money)
│   │       ├── event/                        (Base domain event interface)
│   │       └── security/                     (Security utilities, annotations)
│   │
│   ├── gateway/                              (Spring Cloud Gateway)
│   ├── identity-module/                      (Identity & Access)
│   ├── station-module/                       (Station Management)
│   ├── session-module/                       (Session Management)
│   ├── billing-module/                       (Pricing & Billing)
│   ├── payment-module/                       (Payment Processing)
│   ├── vehicle-module/                       (Vehicle Management)
│   ├── notification-module/                  (Notification)
│   └── device-gateway-module/                (Device Gateway)
│
└── frontend/                                  (React + Vite)
    ├── package.json
    ├── pnpm-workspace.yaml
    ├── vite.config.ts
    └── src/
```

### Evcharging Module (Composition Root)
The `evcharging` module is the **composition root** — it:
- Contains `@SpringBootApplication` main class (`EvChargingApplication.java`)
- Declares `@NamedModule` for Spring Modulith verification
- Configures shared infrastructure (Jackson, Flyway, OpenAPI, Security)
- Loads all `application-{profile}.yaml` configuration files
- Assembles the runtime context by importing all business modules
- Starts the embedded Tomcat / Undertow server
- **Owns no business logic** — purely infrastructure wiring

### Package Convention per Module (Hexagonal)
```
com.evcharging.{module}/
├── domain/
│   ├── model/          (Aggregates, Entities, Value Objects)
│   ├── event/          (Domain Events)
│   ├── repository/     (Port interfaces)
│   └── service/        (Domain services)
├── application/
│   ├── service/        (Application services)
│   ├── dto/            (Application DTOs)
│   └── port/           (Input/Output ports)
├── infrastructure/
│   ├── adapter/        (Adapter implementations)
│   ├── config/         (Module configuration)
│   └── persistence/    (JPA entities, repository impls)
├── api/
│   ├── controller/     (REST controllers)
│   └── dto/            (API DTOs)
└── {ModuleName}Module.java  (Spring Modulith @NamedModule)
```

### Data Storage
| Data | Type | Volume | Retention |
|------|------|--------|-----------|
| Project structure | Filesystem | N/A | N/A |
| Build artifacts | Maven target/ | N/A | Ephemeral |

---

## Constraints

- Follow approved architecture decisions from ADR-0001 and standards
- Spring Boot 4 + Java 21 (as per tech-stack standard)
- React 18 + Vite + TypeScript + pnpm (as per tech-stack standard)
- Modular monolith with Spring Modulith enforcement
- No microservices infrastructure for MVP (single deployable unit)
- PostgreSQL schema-per-module with Flyway
- Hexagonal architecture per module (ports/adapters)

---

## Success Criteria

### Functional
- [ ] Backend project compiles (`mvn clean install` succeeds)
- [ ] Frontend project builds (`pnpm build` succeeds)
- [ ] Spring Modulith module verification test passes
- [ ] PostgreSQL schema-per-module migrations configured
- [ ] Docker build succeeds (multi-stage)
- [ ] Swagger UI accessible at `/swagger-ui.html`
- [ ] JWT RS256 key pair generated

### Quality
- [ ] Clean separation of modules with no cross-dependencies
- [ ] Shared kernel extracted (no duplication of base types)
- [ ] CI pipeline passes

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-000-bootstrap-1 | Simple | S1, S2, S3, S4 | Project scaffolding, build config, module boundaries, CI |

---

## Notes

**Critical**: This is the FIRST unit to execute. All other units depend on the project structure being in place. The bootstrap creates the skeleton — each subsequent unit fills in domain logic within its module. Use `simple-construction-bolt` since this is project setup, not domain logic design.
