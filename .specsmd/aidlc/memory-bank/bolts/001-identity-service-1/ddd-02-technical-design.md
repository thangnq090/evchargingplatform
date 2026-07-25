---
unit: 001-identity-service
bolt: 001-identity-service-1
stage: design
status: complete
updated: "2026-07-24T20:23:32Z"
---

# Technical Design - Identity & Access Service (Bolt 1)

## Architecture Pattern

Clean layered architecture inside a Spring Modulith module. The design strictly isolates domain logic from infrastructure and presentation concerns.
- **Presentation**: REST API controllers and request/response DTOs.
- **Application**: Use cases (Services) coordinating transactions, mappings, and security checks.
- **Domain**: Pure Java domain entities, aggregates, value objects, domain events, and repositories (interfaces).
- **Infrastructure**: Spring Security filter configurations, password hashing, and JPA/Hibernate entities and repository implementations.

## Layer Structure

```text
┌────────────────────────────────────────────────────────┐
│                      Presentation                      │  api.controller, api.dto
├────────────────────────────────────────────────────────┤
│                       Application                      │  application.service, application.dto
├────────────────────────────────────────────────────────┤
│                         Domain                         │  domain.model, domain.event, domain.repository
├────────────────────────────────────────────────────────┤
│                     Infrastructure                     │  infrastructure.persistence, infrastructure.config
└────────────────────────────────────────────────────────┘
```

## API Design

All endpoints wrap responses in the standard `ApiResponse` envelope:

| Endpoint | Method | Request | Response | Description |
|----------|--------|---------|----------|-------------|
| `/api/v1/auth/register-admin` | POST | `RegisterAdminRequest` { name, email, password } | `ApiResponse<UserResponse>` { id, name, email, role, status } | Public endpoint to register the first Platform Administrator. |
| `/api/v1/auth/login` | POST | `LoginRequest` { email, password } | `ApiResponse<LoginResponse>` { accessToken, refreshToken, tokenType, expiresIn } | Public endpoint for credentials verification. Returns access token (15 mins) and refresh token (7 days). |
| `/api/v1/auth/vendors` | POST | `CreateVendorRequest` { vendorName, adminName, adminEmail } | `ApiResponse<CreateVendorResponse>` { vendorId, vendorName, invitationId, invitationToken } | Admin-only endpoint. Creates a vendor and issues an invitation for the primary vendor user. |
| `/api/v1/auth/invitations/accept` | POST | `AcceptInvitationRequest` { token, password } | `ApiResponse<UserResponse>` { id, name, email, role, status } | Public endpoint. Validates invitation token, registers user with VENDOR_ADMIN role, and links them to the vendor. |
| `/api/v1/auth/vendors/users` | POST | `AddVendorUserRequest` { name, email, role } | `ApiResponse<UserResponse>` { id, name, email, role, status } | VENDOR_ADMIN-only endpoint. Adds a secondary VENDOR_USER to the same vendor. |

## Data Persistence

Tables created in the `identity` PostgreSQL schema. Flyway migrations will manage schema creation.

| Table | Columns | Relationships |
|-------|---------|---------------|
| `identity.vendors` | `id` UUID PK<br>`name` VARCHAR(100) UNIQUE NOT NULL<br>`status` VARCHAR(20) NOT NULL<br>`created_at` TIMESTAMP NOT NULL<br>`updated_at` TIMESTAMP NOT NULL | One-to-Many with `identity.users`<br>One-to-Many with `identity.invitations` |
| `identity.users` | `id` UUID PK<br>`name` VARCHAR(100) NOT NULL<br>`email` VARCHAR(255) UNIQUE NOT NULL<br>`password_hash` VARCHAR(255) NOT NULL<br>`role` VARCHAR(20) NOT NULL<br>`vendor_id` UUID FK -> `identity.vendors(id)` NULLABLE<br>`status` VARCHAR(20) NOT NULL<br>`created_at` TIMESTAMP NOT NULL<br>`updated_at` TIMESTAMP NOT NULL | Many-to-One with `identity.vendors` |
| `identity.invitations` | `id` UUID PK<br>`email` VARCHAR(255) NOT NULL<br>`vendor_id` UUID FK -> `identity.vendors(id)` NOT NULL<br>`role` VARCHAR(20) NOT NULL<br>`token` VARCHAR(255) UNIQUE NOT NULL<br>`expires_at` TIMESTAMP NOT NULL<br>`status` VARCHAR(20) NOT NULL<br>`created_at` TIMESTAMP NOT NULL | Many-to-One with `identity.vendors` |

## Security Design

| Concern | Approach |
|---------|----------|
| **Authentication** | Spring Security + stateless JWT using JWT service. JWT contains claims for `userId`, `email`, `role`, and `vendorId` (if applicable). |
| **Authorization** | Method-level security annotations (`@PreAuthorize`) in controllers and services. Role-based Access Control (RBAC): `ADMIN` can create vendors; `VENDOR_ADMIN` can add users to their own vendor. |
| **Data Encryption** | Passwords hashed using `BCryptPasswordEncoder` (strength: 12) before storage. Data in transit secured via TLS. |

## NFR Implementation

| Requirement | Design Approach |
|-------------|-----------------|
| **Performance** | Database indexes on lookup columns: `users(email)`, `vendors(name)`, `invitations(token)`. Stateless JWT verification is highly efficient and minimizes DB lookups. |
| **Scalability** | Avoid server-side sessions. Scoped queries (e.g. searching/listing user profiles) are constrained to `vendorId` from the JWT claims to prevent full table scans. |
| **Reliability** | Database actions wrapped in `@Transactional` at the Service layer. Use transactional outbox pattern or direct Spring Modulith event publisher for publishing domain events. |

## Error Handling

| Error Type | Code | Response | Description |
|------------|------|----------|-------------|
| Validation Failure | `VALIDATION_FAILED` (400) | `ApiResponse.error` with details array | Invalid request format or missing/invalid fields. |
| Unauthenticated | `UNAUTHORIZED` (401) | `ApiResponse.error` | Expired/invalid JWT or wrong credentials. |
| Forbidden | `FORBIDDEN` (403) | `ApiResponse.error` | Insufficient permissions (e.g., VENDOR_ADMIN accessing admin resources). |
| Conflict | `DUPLICATE_RESOURCE` (409) | `ApiResponse.error` | Email or vendor name already exists. |
| Business Exception | `INVITATION_EXPIRED` (400) | `ApiResponse.error` | Action fails due to business invariants (e.g., invitation token expired). |

## External Dependencies

| Service | Purpose | Integration |
|---------|---------|-------------|
| PostgreSQL (identity schema) | Persistent store for users, vendors, and invitations. | JDBC / Spring Data JPA |
| Spring Boot starter security | Core authorization and password hashing. | In-process Framework |
