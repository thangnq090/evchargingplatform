# Coding Standards

## Overview
Standards for the EV Charging Platform ensuring consistent, maintainable, and testable code across the modular monolith. Applied to both Spring Boot (backend) and React/TypeScript (frontend). Enforced via automated tooling in CI.

## Code Formatting

### Backend (Java)
**Tool**: `google-java-format` (via Maven plugin `spotless-maven-plugin`)
**Key Settings**:
- Line length: 100 characters
- Indentation: 2 spaces
- Import order: java, javax, org, com, then static
- Trailing commas: Not applicable

**Enforcement**: 
- `mvn spotless:check` in CI (fails build on formatting issues)
- `mvn spotless:apply` to auto-fix locally
- Pre-commit hook recommended

### Frontend (TypeScript/React)
**Tool**: Prettier 3.x
**Key Settings**:
- Line length: 100 characters
- Indentation: 2 spaces
- Single quotes: true
- Trailing commas: es5 (multi-line)
- Semi-colons: true
- Print width: 100
- Tab width: 2
- Arrow parens: avoid
- End of line: lf

**Enforcement**:
- `pnpm prettier:check` in CI
- `pnpm prettier:write` to auto-fix locally
- Pre-commit hook via Husky
- VS Code: Format on Save enabled

## Linting

### Backend (Java)
**Tool**: Checkstyle + Error Prone (via Maven)
**Base Config**: Google Checks + Spring Boot conventions
**Strictness**: Strict (fail build on violations)

**Key Rules**:
- `JavadocMethod`: Require Javadoc on public APIs
- `MissingOverride`: Warn on missing @Override
- `AvoidInlineConditionals`: Prefer explicit if/else
- `MagicNumber`: No magic numbers (use constants)
- `CyclomaticComplexity`: Max 10 per method
- `NPathComplexity`: Max 200 per method
- Error Prone: `BugPattern` checks enabled

### Frontend (TypeScript/React)
**Tool**: ESLint 9.x with TypeScript ESLint
**Base Config**: 
- `eslint:recommended`
- `plugin:@typescript-eslint/recommended-type-checked`
- `plugin:@typescript-eslint/strict-type-checked`
- `plugin:react/recommended`
- `plugin:react-hooks/recommended`
- `prettier` (disable conflicting rules)

**Strictness**: Strict (fail build on errors, warnings allowed)

**Key Rules**:
- `@typescript-eslint/no-explicit-any`: error (use `unknown`)
- `@typescript-eslint/no-unused-vars`: error (argsIgnorePattern: "^_")
- `@typescript-eslint/consistent-type-imports`: error
- `@typescript-eslint/no-floating-promises`: error
- `react-hooks/exhaustive-deps`: error
- `react/no-unescaped-entities`: off (JSX handles this)
- `import/order`: error (groups: builtin, external, internal, parent, sibling, index)

## Naming Conventions

### Backend (Java)

| Element | Convention | Example |
|---------|------------|---------|
| Packages | lowercase, reverse domain | `com.evcharging.session` |
| Classes/Interfaces | PascalCase | `SessionService`, `SessionRepository` |
| Methods | camelCase | `startSession`, `calculateCost` |
| Fields/Variables | camelCase | `sessionId`, `isActive` |
| Constants | UPPER_SNAKE_CASE | `MAX_SESSION_DURATION_MINUTES` |
| Enums | PascalCase | `SessionStatus`, `ConnectorType` |
| Enum values | UPPER_SNAKE_CASE | `CHARGING`, `COMPLETED` |
| Test classes | `*Test` suffix | `SessionServiceTest` |
| Test methods | `should_ExpectedBehavior_when_State` | `shouldStartSessionWhenConnectorAvailable` |

**Special Conventions**:
- DDD Aggregates: `*Aggregate` (rare, usually just Entity name)
- Domain Events: `*Event` suffix (e.g., `SessionStartedEvent`)
- Value Objects: Noun describing concept (e.g., `Money`, `Tariff`, `StationId`)
- Repository Interfaces: `*Repository`
- Domain Services: `*Service` (in domain layer)
- Application Services: `*Service` (in application layer)
- Adapters: `*Adapter` (implementing ports)
- Controllers: `*Controller` (REST endpoints)
- DTOs: `*Request`, `*Response`, `*DTO`

### Frontend (TypeScript/React)

| Element | Convention | Example |
|---------|------------|---------|
| Variables | camelCase | `sessionId`, `isCharging` |
| Functions | camelCase | `formatCurrency`, `calculateDuration` |
| Classes | PascalCase | `ApiClient`, `SessionManager` |
| Interfaces | PascalCase | `Session`, `StartSessionRequest` |
| Types | PascalCase | `SessionStatus`, `ApiResponse` |
| Type Parameters | PascalCase (single letter for generics) | `<T>`, `<TRequest, TResponse>` |
| Constants | UPPER_SNAKE_CASE | `API_BASE_URL`, `MAX_RETRIES` |
| React Components | PascalCase | `SessionCard`, `StationMap` |
| React Hooks | camelCase with `use` prefix | `useSession`, `useAuth` |
| Component Props Interfaces | `ComponentNameProps` | `SessionCardProps` |
| Event Handlers | `handle` + Event | `handleStartClick`, `handleSessionEnd` |
| Boolean Variables | `is`, `has`, `can`, `should` | `isActive`, `hasPermission` |
| Files (Components) | PascalCase | `SessionCard.tsx` |
| Files (Hooks) | camelCase with `use` | `useSession.ts` |
| Files (Utils/Types) | kebab-case | `date-utils.ts`, `api-types.ts` |
| Test Files | `*.test.ts` / `*.test.tsx` | `SessionCard.test.tsx` |

## File Organization

### Backend (Spring Boot Modular Monolith)

**Pattern**: Module-based (DDD-aligned modules per ADR-0001)

```
src/main/java/com/evcharging/
├── identity/                    # Identity & Access module
│   ├── domain/
│   │   ├── model/              # Aggregates, Entities, Value Objects
│   │   ├── event/              # Domain Events
│   │   ├── repository/         # Repository interfaces (Ports)
│   │   └── service/            # Domain Services
│   ├── application/
│   │   ├── service/            # Application Services (Use Cases)
│   │   ├── dto/                # Application DTOs
│   │   └── port/               # Input/Output Ports
│   ├── infrastructure/
│   │   ├── adapter/            # Adapters (JPA, REST clients, etc.)
│   │   ├── config/             # Module configuration
│   │   └── persistence/        # JPA Entities, Repositories (Adapters)
│   ├── api/
│   │   ├── controller/         # REST Controllers
│   │   └── dto/                # API Request/Response DTOs
│   └── IdentityModule.java     # Spring Modulith module descriptor
├── station/                     # Station Management module
├── session/                     # Session Management module
├── billing/                     # Pricing & Billing module
├── payment/                     # Payment Processing module
├── notification/                # Notification module
├── devicegateway/               # Device Gateway module
├── shared/                      # Shared kernel (reference data only)
│   ├── kernel/                  # Shared value objects, IDs
│   └── security/                # Shared security utilities
└── EvChargingApplication.java   # Main application class
```

**Test Structure** (mirrors main):
```
src/test/java/com/evcharging/
├── identity/
│   ├── domain/
│   ├── application/
│   └── api/
```

**Key Conventions**:
- One module per top-level package under `com.evcharging`
- Module descriptor: `{ModuleName}Module.java` with `@NamedModule`
- Domain layer has NO Spring dependencies (pure Java)
- Application layer uses Spring for transactions only
- Infrastructure layer contains all framework-specific code
- API layer: Controllers + API DTOs only
- Shared kernel: Only value objects and IDs referenced by multiple modules

### Frontend (React + Vite)

**Pattern**: Feature-based with shared kernel

```
src/
├── features/                    # Feature modules (aligned with backend modules)
│   ├── auth/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── api/
│   │   ├── types.ts
│   │   └── index.ts
│   ├── stations/
│   ├── sessions/
│   ├── billing/
│   └── notifications/
├── shared/                      # Shared code across features
│   ├── components/              # Generic UI components (Button, Input, Modal)
│   ├── hooks/                   # Shared hooks (useAuth, useApi)
│   ├── utils/                   # Pure utility functions
│   ├── types/                   # Shared type definitions
│   ├── constants/               # Shared constants
│   └── styles/                  # Global styles, theme, CSS variables
├── app/                         # App-level setup
│   ├── routes/                  # Routing configuration
│   ├── providers/               # Context providers (Auth, QueryClient)
│   └── layout/                  # Layout components (Header, Sidebar)
├── pages/                       # Page components (route-level)
│   ├── Dashboard.tsx
│   ├── Stations.tsx
│   └── Sessions.tsx
├── main.tsx                     # Entry point
├── App.tsx                      # Root component
└── vite-env.d.ts               # Vite type declarations
```

**Conventions**:
- Feature folders mirror backend module names
- Colocate types with features (`types.ts` in each feature)
- Barrel exports via `index.ts` in each feature
- Shared components are generic/reusable; feature components are specific
- Pages compose features; no business logic in pages
- Test files: `*.test.tsx` co-located with source

## Testing Strategy

### Backend

**Framework**: JUnit 5 + Mockito + AssertJ + Testcontainers
**Coverage Target**: 80% line coverage (enforced by JaCoCo in CI)

**Test Types**:

| Type | Tool | When to Use | Location |
|------|------|-------------|----------|
| Unit | JUnit 5, Mockito | Domain logic, services, utilities | `src/test/.../domain/`, `application/` |
| Integration | Spring Boot Test, Testcontainers | Repository adapters, Controllers, Module integration | `src/test/.../infrastructure/`, `api/` |
| Contract | Spring Cloud Contract / REST Assured | API contracts, Inter-module events | Separate module or `contract/` |

**Conventions**:
- Test naming: `should_ExpectedBehavior_when_StateUnderTest`
- Structure: Given-When-Then (explicit comments or helper methods)
- One assertion per test (multiple related assertions OK)
- Use `@Nested` for grouping related tests
- Test data: Builders/Factory methods (not fixtures)
- Mock at boundaries only (ports/adapters), not internal collaborators
- Testcontainers for PostgreSQL, Redis in integration tests
- `@SpringBootTest` only for integration tests; unit tests use plain JUnit

### Frontend

**Framework**: Vitest + React Testing Library + MSW (Mock Service Worker)
**Coverage Target**: 80% line coverage (enforced by Vitest in CI)

**Test Types**:

| Type | Tool | When to Use | Location |
|------|------|-------------|----------|
| Unit | Vitest | Pure functions, hooks, utilities | Co-located `*.test.ts` |
| Component | Vitest + RTL | React components in isolation | Co-located `*.test.tsx` |
| Integration | Vitest + MSW | Feature flows, API integration | Co-located or `features/*/__tests__/` |
| E2E | Playwright | Critical user journeys | `e2e/` (separate) |

**Conventions**:
- Test naming: `it('should ...')` or `describe('when ..., it should ...')`
- Render with providers via custom `renderWithProviders` wrapper
- MSW handlers in `src/mocks/handlers.ts`
- Test user interactions (fireEvent, userEvent) not implementation
- Snapshot testing: Only for stable UI components, opt-in

## Error Handling

### Backend (Java)

**Pattern**: Structured exceptions with ProblemDetail (RFC 7807)

**Exception Hierarchy**:
```
EvChargingException (abstract)
├── DomainException          # Business rule violations
│   ├── SessionNotFoundException
│   ├── InsufficientBalanceException
│   └── StationOfflineException
├── ValidationException      # Input validation failures
├── ConflictException        # Optimistic locking, idempotency conflicts
├── UnauthorizedException    # Authentication failures
├── ForbiddenException       # Authorization failures
└── ExternalServiceException # Downstream service failures
    ├── PaymentProviderException
    └── NotificationProviderException
```

**Global Handler**: `@RestControllerAdvice` converting to `ProblemDetail`
- Includes: `type`, `title`, `status`, `detail`, `instance`, `timestamp`
- Extensions: `errorCode`, `fieldErrors` (for validation)
- Logging: ERROR level with full stack trace; correlation ID included

**Domain Layer**: Throw domain exceptions from aggregates/services
**Application Layer**: Catch domain exceptions, map to appropriate response
**Infrastructure Layer**: Wrap external exceptions in `ExternalServiceException`

### Frontend (TypeScript)

**Pattern**: Result type for expected errors, exceptions for unexpected

```typescript
// Expected errors (validation, not found, business rules)
type Result<T, E = AppError> = 
  | { ok: true; value: T }
  | { ok: false; error: E };

// AppError structure
interface AppError {
  code: string;           // Machine-readable (e.g., "SESSION_NOT_FOUND")
  message: string;        // Human-readable
  status?: number;        // HTTP status if from API
  fieldErrors?: Record<string, string>; // Validation errors
}
```

**API Client**: Returns `Result<T, AppError>` from all calls
**Components**: Handle `ok: false` with user-friendly messages
**Unexpected Errors**: Throw, caught by React Error Boundary
**Error Boundary**: Logs to monitoring, shows fallback UI

## Logging

### Backend (Java)

**Tool**: SLF4J + Logback (Spring Boot default)
**Format**: Structured JSON (Logstash/ECS compatible)

**Configuration**:
```xml
<encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
  <providers>
    <timestamp><timeZone>UTC</timeZone></timestamp>
    <level/>
    <loggerName/>
    <message/>
    <mdc/>  <!-- Includes correlationId, sessionId, stationId -->
    <stackTrace/>
  </providers>
</encoder>
```

**Levels**:
| Level | Usage |
|-------|-------|
| ERROR | System errors, exceptions, failed operations |
| WARN  | Recoverable issues, deprecated usage, circuit breaker open |
| INFO  | Business events (session started, payment completed), request/response summary |
| DEBUG | Detailed flow, SQL parameters, external API calls |
| TRACE | Very verbose (disabled in prod) |

**MDC Context** (auto-populated via Filter/Interceptor):
- `correlationId`: Request trace ID (propagated via headers)
- `sessionId`: Charging session ID (when applicable)
- `stationId`: Station ID (when applicable)
- `userId`: Authenticated user ID

**Rules**:
- Always log: Authentication events, payment events, session state changes, errors with context
- Never log: Passwords, tokens, API keys, PAN, PII (use structured masking)
- Use parameterized logging: `log.info("Session {} started for station {}", sessionId, stationId)`
- Correlation ID propagated via `X-Correlation-ID` header; generated at Gateway if missing

### Frontend (TypeScript)

**Tool**: Structured console logging (dev) → OpenTelemetry logs (prod)
**Format**: JSON in production, pretty in development

**Levels**:
| Level | Usage |
|-------|-------|
| error | Uncaught errors, API failures, critical failures |
| warn  | Deprecated APIs, fallback behavior, retry attempts |
| info  | User actions (login, session start), navigation |
| debug | API requests/responses, state changes |

**Rules**:
- Use `logger.info('eventName', { key: value })` structured format
- Include `correlationId` from response headers
- Never log sensitive data (tokens, passwords)
- Production: Batch and send via OTel log exporter

## Decision Relationships

- **Tech Stack → Formatting**: Java → google-java-format; TypeScript → Prettier
- **Tech Stack → Linting**: Spring Boot → Checkstyle/Error Prone; React → ESLint + TypeScript ESLint
- **Architecture (ADR-0001) → File Org**: Modular monolith → module-based backend, feature-based frontend
- **Architecture (ADR-0001) → Testing**: Module boundaries → integration tests per module, contract tests for events
- **Domain-Driven Design → Naming**: Aggregates, Events, Value Objects, Repositories follow DDD terminology
- **Observability (ADR-009) → Logging**: OpenTelemetry + structured JSON + correlation IDs
- **Error Handling → API Conventions**: RFC 7807 ProblemDetail for consistent error responses