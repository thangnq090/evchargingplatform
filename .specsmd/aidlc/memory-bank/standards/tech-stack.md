# Tech Stack

## Overview
Full-stack EV charging platform using a modular monolith architecture (ADR-0001). Frontend built with React + Vite for fast development and HMR. Backend built with Spring Boot for robust enterprise-grade API services. Both deployed as a modular monolith to Kubernetes.

## Languages

**Frontend**: TypeScript
- Type safety catches bugs early
- Excellent React/Vite ecosystem
- Strong tooling (ESLint, Prettier, Vitest)

**Backend**: Java 21 (LTS)
- Spring Boot 4.x ecosystem
- Virtual threads for high concurrency
- Strong enterprise support and observability
- Team expertise in Java/Spring

## Framework

**Frontend**: React 18 + Vite
- Fast development server with HMR
- Modern React patterns (hooks, concurrent features)
- Rich component ecosystem
- Easy deployment as static assets

**Backend**: Spring Boot 4.x (latest stable)
- Spring WebFlux for reactive endpoints where needed
- Spring MVC for traditional REST
- Spring Data JPA + Hibernate
- Spring Security for authentication/authorization
- Spring Modulith for modular monolith enforcement
- OpenAPI 3 generation via springdoc

## Authentication

**Solution**: Spring Security + JWT (stateless) with OAuth2/OIDC support
- Access tokens: Short-lived JWT (15 min)
- Refresh tokens: Rotating, stored in httpOnly cookies
- OAuth2 Resource Server for token validation
- Integration ready for Keycloak/Auth0 as identity provider
- Role-based access control (RBAC) with Spring Security annotations

## Infrastructure & Deployment

**Platform**: Kubernetes (EKS/GKE/AKS)
- Helm charts for deployment
- Modular monolith packaged as single container image
- Horizontal pod autoscaling (HPA) based on CPU/memory
- Multi-AZ deployment for HA

**Database**: PostgreSQL 16 (managed - Cloud SQL / RDS / Cloud SQL)
- Primary database for all modules
- Schema-per-module for modular monolith boundaries
- Flyway for migrations

**Messaging**: In-process events (Spring Modulith ApplicationEventPublisher)
- For modular monolith internal communication
- Transactional event publishing
- Evolution path to Kafka for distributed deployment

**Caching**: Redis (managed - ElastiCache / Memorystore)
- Session storage (refresh tokens)
- Rate limiting
- Distributed cache for reference data

**Observability**:
- OpenTelemetry Java agent (auto-instrumentation)
- Micrometer + Prometheus metrics
- Structured JSON logging (Logstash/ECS format)
- Grafana dashboards
- Distributed tracing with Tempo/Jaeger

**CI/CD**: GitHub Actions
- Build: Maven (backend), Vite (frontend)
- Test: Unit + integration tests in pipeline
- Container: Build multi-arch Docker image
- Deploy: Helm upgrade via ArgoCD / Flux

## Package Manager

**Frontend**: pnpm (workspaces for monorepo)
- Fast, disk-efficient
- Excellent monorepo support

**Backend**: Maven (standard for Spring Boot)
- Wrapper included (mvnw)

## Decision Relationships

- **Languages → Frameworks**: TypeScript enables React/Vite; Java 21 enables Spring Boot 4
- **Framework → Auth**: Spring Security integrates natively with Spring Boot
- **Framework → Infrastructure**: Spring Boot 4 + GraalVM native image ready for containers
- **Architecture (ADR-0001) → Modularity**: Spring Modulith enforces module boundaries
- **Database → ORM**: PostgreSQL + Spring Data JPA + Hibernate
- **Messaging → Architecture**: In-process events align with modular monolith; Kafka as evolution path