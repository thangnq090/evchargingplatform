---
stage: plan
bolt: 000-project-bootstrap-1
created: "2026-07-24T15:15:00Z"
---

## Implementation Plan: 000-project-bootstrap

### Objective
Create the foundational project structure for the EV Charging Platform modular monolith, including backend (Spring Boot 4 + Maven multi-module), frontend (React 18 + Vite + TypeScript + pnpm), Docker multi-stage build, GitHub Actions CI pipeline, and Spring Cloud Gateway with JWT RS256 authentication.

### Deliverables

#### Backend Scaffolding (Story 000-001)
- Parent POM with centralized dependency management and plugin configuration
- `evcharging-app` composition root module:
  - Spring Boot main application class (`EvChargingApplication`)
  - Spring Modulith module configuration (`EvChargingModule` with `@NamedModule`)
  - Global application configuration (Jackson, Flyway multi-schema, OpenAPI, Security)
  - Infrastructure bootstrap and runtime assembly
- 8 domain modules (each with independent ownership):
  - `identity-module`
  - `station-module`
  - `session-module`
  - `billing-module`
  - `payment-module`
  - `vehicle-module`
  - `notification-module`
  - `device-gateway-module`
- Each domain module follows hexagonal architecture:
  ```
  {module}/
  ├── domain/
  │   ├── model/       (Aggregates, Entities, Value Objects - OWNED by module)
  │   ├── service/     (Domain Services)
  │   ├── event/       (Domain Events)
  │   └── repository/  (Repository Port Interfaces)
  ├── application/
  │   ├── usecase/     (Application Services / Use Cases)
  │   └── command/query/ (Commands & Queries)
  ├── infrastructure/
  │   ├── persistence/      (JPA Entities, Repository Adapters)
  │   ├── external-adapter/ (External API clients, adapters)
  │   └── configuration/    (Module Spring config)
  └── api/
      ├── controller/ (REST Controllers)
      └── dto/        (API Request/Response DTOs)
  ```
- Each module includes:
  - Spring Modulith module descriptor (`{ModuleName}Module.java` with `@NamedModule`)
  - Independent Flyway migration scripts: `db/migration/{module}/`
- **NO shared-kernel module** — each module owns its domain identifiers
- Cross-module references use UUID primitives only
- Shared technical abstractions only (in `evcharging-app`):
  - Common error handling (ProblemDetail, exception hierarchy)
  - API conventions
  - Correlation ID utilities
  - Security primitives
  - Logging/tracing utilities

#### Gateway Module (Story 000-001 continued)
- `gateway-module`:
  - Spring Cloud Gateway configuration
  - API routing to module controllers
  - Authentication filter integration (JWT validation)
  - Rate limiting hooks (future)
  - CORS/security configuration

#### API Documentation (Story 000-001 continued)
- Springdoc OpenAPI configuration in `evcharging-app`
- Swagger UI enabled at `/swagger-ui.html`
- API versioning convention: `/api/v1/{resource}`

#### Frontend Scaffolding (Story 000-002)
- pnpm workspace root with `package.json`, `pnpm-workspace.yaml`
- Vite + React + TypeScript app structure
- Feature-based folder structure mirroring backend modules: `auth`, `stations`, `sessions`, `billing`, `vehicles`, `admin`
- Shared kernel: `shared/components`, `shared/hooks`, `shared/utils`, `shared/types`, `shared/constants`, `shared/styles`
- ESLint + Prettier + TypeScript config
- TanStack Query + React Router setup
- Tailwind CSS with design tokens

#### CI/Docker Scaffolding (Story 000-003)
- Multi-stage Dockerfile: Maven build → distroless Java 21 runtime
- `docker-compose.yml`: app + PostgreSQL (PostGIS) + Redis
- GitHub Actions workflow (`.github/workflows/ci.yaml`): build → test → scan (Trivy) → build image
- Health endpoint configuration

#### JWT Gateway Scaffolding (Story 000-004)
- Spring Cloud Gateway routes to module controllers
- OAuth2 Resource Server with JWT validation (RS256)
- JWKS endpoint exposing public key
- OpenSSL script for RS256 key pair generation (`scripts/generate-jwt-keys.sh`)

### Dependencies
- **External**: Maven/Java 21, pnpm/Node.js, Docker, PostgreSQL 16+PostGIS, Redis
- **Internal**: None (foundational bolt)

### Technical Approach
1. Generate Spring Boot project structure using Spring Initializr as base, then convert to multi-module
2. Create all module POMs with proper dependencies and Spring Modulith configuration
3. Scaffold hexagonal package structure in each module
4. Set up shared technical utilities in composition root
5. Create Vite + React + TypeScript project with pnpm
6. Configure ESLint/Prettier/Tailwind
7. Write Dockerfile and docker-compose
8. Create GitHub Actions workflow
9. Configure Spring Cloud Gateway + JWT
10. Write key generation script

### Acceptance Criteria
- [ ] `mvn clean install` succeeds in backend directory
- [ ] 8 domain modules + gateway + app module all build independently
- [ ] Spring Modulith verification test passes (no forbidden cross-module dependencies)
- [ ] Each domain module owns its domain model — no shared-kernel domain objects
- [ ] Cross-module references use UUID primitives only
- [ ] `pnpm install && pnpm build` succeeds in frontend directory
- [ ] Docker image builds successfully (~100MB distroless)
- [ ] `docker-compose up` starts app + PostgreSQL + Redis
- [ ] Swagger UI accessible at `/swagger-ui.html`
- [ ] JWT RS256 key pair generated via script
- [ ] JWKS endpoint returns public key
- [ ] Unauthenticated requests to gateway return 401