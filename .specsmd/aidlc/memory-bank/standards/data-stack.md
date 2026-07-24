# Data Stack

## Overview
PostgreSQL with schema-per-module architecture aligned with the modular monolith (ADR-0001). Spring Data JPA + Hibernate for type-safe, domain-driven persistence. Flyway for versioned migrations per module. JSR 354 (Moneta) for monetary values.

## Database

**PostgreSQL 16** (managed - Cloud SQL / RDS / Azure Database for PostgreSQL)

**Architecture**: Single instance, schema-per-module
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

**Rules** (enforced by DB permissions + ArchUnit tests):
- Module code only accesses its own schema
- Cross-module queries forbidden; use domain events + eventual consistency
- Reference data (e.g., station_id in session) = foreign key to other schema, read-only from owning module
- Flyway migrations per module (`db/migration/session/V1__...`)

**Scaling**:
- Read replicas for reporting/analytics
- Connection pooling: HikariCP (Spring Boot default)
- PgBouncer for connection multiplexing at scale

**Managed Service Benefits**:
- Automated backups, point-in-time recovery
- Multi-AZ HA with automatic failover
- Encryption at rest + in transit
- Maintenance windows, minor version upgrades

## ORM / Database Client

**Spring Data JPA + Hibernate 6.x**

**Why this combination**:
- Type-safe repository interfaces with query derivation
- Domain-driven design support (Aggregates, Entities, Value Objects)
- JPA 3.1 + Hibernate 6.2+ for Java 21 virtual thread compatibility
- Spring Modulith integration for module boundary enforcement
- Mature ecosystem, extensive documentation, team expertise

**Configuration Highlights**:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway manages schema
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        jdbc.time_zone: UTC
```

**Domain Modeling Patterns**:
- **Aggregates**: Root entity with `@AggregateRoot` (Spring Modulith)
- **Entities**: `@Entity` with `@Table(schema = "module_name")`
- **Value Objects**: `@Embeddable` (Address, Money, TariffRule)
- **Domain Events**: `@DomainEvents` methods on aggregates
- **Repositories**: `JpaRepository<Entity, ID>` + custom queries via `@Query`

**Monetary Values**: JSR 354 (`org.javamoney:moneta`)
- Never raw `BigDecimal` for money
- `MonetaryAmount` with `CurrencyUnit` for type-safe calculations
- Custom `AttributeConverter` for JPA persistence

**Migration Strategy**: Flyway
- Per-module migration directories: `db/migration/{module}/`
- Naming: `V{version}__{description}.sql`
- Baseline on existing schema if migrating
- Test migrations in CI against test containers

## Decision Relationships

- **Tech Stack → Database**: Java 21 + Spring Boot 4 → Spring Data JPA + Hibernate 6 + PostgreSQL
- **Architecture (ADR-0001) → Data**: Modular monolith → schema-per-module, no cross-schema joins
- **Architecture (ADR-0001) → Messaging**: In-process domain events (Spring Modulith ApplicationEventPublisher)
- **Billing (ADR-008) → Money**: JSR 354 Moneta for all monetary calculations
- **Infrastructure → Database**: Managed PostgreSQL + Flyway = operational simplicity