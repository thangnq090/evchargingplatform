# Story: Backend Project Scaffolding with Maven Multi-Module

## User Story
As a **Developer**
I want to **scaffold the Spring Boot 4 modular monolith project with Maven multi-module structure**
So that **all module teams can work within consistent project conventions from day one**

## Acceptance Criteria
- [ ] Given Maven multi-module project, When built, Then `mvn clean install` compiles all modules
- [ ] Given 8 domain modules + shared-kernel + gateway, When module structure is defined, Then each has `domain`, `application`, `infrastructure`, `api` packages
- [ ] Given Spring Modulith, When module verification test runs, Then it passes (no forbidden cross-dependencies)
- [ ] Given shared-kernel module, When referenced, Then it contains UserId, StationId, Money value objects
- [ ] Given Flyway configuration, When migrations are located, Then they are per-module under `db/migration/{module}/`

## Technical Notes
- Parent POM with dependency management
- spring-modulith-starter-core for module enforcement
- springdoc-openapi for API docs
- PostGIS dependency in station module

## Dependencies
- None (first story — foundational)
