---
stage: design
bolt: 003-identity-service-3
created: "2026-07-25T07:38:00Z"
---

# Technical Design: 003-identity-service-3 — RBAC & Credential Management

## 1. Architecture Pattern

**Hexagonal Architecture (Ports & Adapters)** — consistent with bolts 001 and 002.

```text
┌──────────────────────────────────────────────────────────┐
│                  Presentation Layer                       │
│   IdentityController (modified — new endpoints)           │
│   UserManagementController (new)                          │
├──────────────────────────────────────────────────────────┤
│                  Application Layer                        │
│   CredentialManagementApplicationService (new)            │
│   RefreshTokenApplicationService (new)                    │
│   AuthenticationApplicationService (modified)             │
├──────────────────────────────────────────────────────────┤
│                    Domain Layer                           │
│   User (modified), RefreshToken (new)                     │
│   Permission (enum), RolePermissionMapping                │
│   CredentialManagementDomainService                       │
│   RefreshTokenDomainService                               │
│   UserRepository (extended), RefreshTokenRepository (new) │
├──────────────────────────────────────────────────────────┤
│                Infrastructure Layer                       │
│   UserRepositoryAdapter (extended)                        │
│   RefreshTokenRepositoryAdapter (new)                     │
│   UserDbEntity (modified), RefreshTokenDbEntity (new)     │
│   IdentitySecurityConfig (modified — new endpoint rules)  │
└──────────────────────────────────────────────────────────┘
```

---

## 2. Layer Responsibilities

| Layer | Package | Responsibility |
|-------|---------|---------------|
| Presentation | `identity.api.controller` | HTTP endpoints, request/response mapping, `@PreAuthorize` |
| Application | `identity.application.service` | Use case orchestration, `@Transactional` boundaries |
| Domain | `identity.domain.*` | Business rules, invariants, domain events — pure Java, no Spring |
| Infrastructure | `identity.infrastructure.*` | JPA adapters, Spring Security config, DB entities |

---

## 3. API Design

All under `/api/v1/identity`. Spring WebFlux reactive (`Mono`). All responses use `ApiResponse<T>` envelope.

### 3.1 Reset User Password (Admin only)

```
POST /api/v1/identity/users/{userId}/password/reset
Authorization: Bearer <ADMIN_JWT>

Response 200:
{
  "data": {
    "userId": "uuid",
    "temporaryPassword": "Tx7k#mP2",
    "mustChangePassword": true,
    "message": "Temporary password issued. User must change on next login."
  },
  "meta": { "timestamp": "...", "version": "v1" }
}
```

Security: `@PreAuthorize("hasRole('ADMIN')")`
Note: Temporary password returned once in response, never logged.

### 3.2 Change Own Password

```
POST /api/v1/identity/users/me/password
Authorization: Bearer <any JWT>

Request: { "currentPassword": "old", "newPassword": "NewPass1!" }

Response 200:
{
  "data": { "message": "Password changed successfully." },
  "meta": { "timestamp": "...", "version": "v1" }
}
```

Security: Any authenticated user (userId extracted from JWT `sub` claim).

### 3.3 Token Refresh

```
POST /api/v1/identity/auth/refresh

Request: { "refreshToken": "<opaque_token>" }

Response 200:
{
  "data": {
    "accessToken": "<new_jwt>", "expiresIn": 900,
    "refreshToken": "<new_opaque_token>",
    "userId": "uuid", "role": "CUSTOMER",
    "mustChangePassword": false
  },
  "meta": { "timestamp": "...", "version": "v1" }
}
```

Security: Public (no JWT required). Refresh token is the credential.
Reuse detection: presented revoked token → 401 + all user tokens revoked.

### 3.4 Logout

```
POST /api/v1/identity/auth/logout
Authorization: Bearer <JWT>

Response 204: No Content
```

Security: Authenticated. Revokes all active refresh tokens for the calling user.

### 3.5 List Users

```
GET /api/v1/identity/users?role=VENDOR_USER&limit=20&cursor=...
Authorization: Bearer <ADMIN or VENDOR_ADMIN JWT>

Response 200:
{
  "data": [
    { "id": "uuid", "name": "Jane", "email": "jane@co.com",
      "role": "VENDOR_USER", "vendorId": "uuid", "status": "ACTIVE",
      "mustChangePassword": false, "createdAt": "..." }
  ],
  "meta": { "timestamp": "...", "pagination": { "cursor": "...", "limit": 20, "hasMore": false } }
}
```

Security: ADMIN sees all; VENDOR_ADMIN sees own vendor users only (vendorId enforced from JWT, not query param).

### Security Matrix

| Endpoint | ADMIN | VENDOR_ADMIN | VENDOR_USER | CUSTOMER |
|----------|-------|--------------|-------------|---------|
| POST /users/{id}/password/reset | ✅ | ❌ | ❌ | ❌ |
| POST /users/me/password | ✅ | ✅ | ✅ | ✅ |
| POST /auth/refresh | public | public | public | public |
| POST /auth/logout | ✅ | ✅ | ✅ | ✅ |
| GET /users | ✅ all | ✅ own vendor | ❌ | ❌ |

---

## 4. Data Model

### 4.1 Migration: V7__add_rbac_credential_management.sql

```sql
-- Add mustChangePassword flag
ALTER TABLE identity.users
  ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- New refresh_tokens table
CREATE TABLE identity.refresh_tokens (
  id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id              UUID         NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
  token_hash           VARCHAR(64)  NOT NULL UNIQUE,
  issued_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  expires_at           TIMESTAMPTZ  NOT NULL,
  revoked_at           TIMESTAMPTZ,
  replaced_by_token_id UUID         REFERENCES identity.refresh_tokens(id),
  user_agent           VARCHAR(512),
  ip_address           VARCHAR(45)
);

CREATE INDEX idx_refresh_tokens_user_id    ON identity.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON identity.refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_active
  ON identity.refresh_tokens(user_id, expires_at)
  WHERE revoked_at IS NULL;
```

### 4.2 Current `identity.users` Schema (post-V7)

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | PK |
| `name` | VARCHAR(100) | |
| `email` | VARCHAR(255) | UNIQUE |
| `password_hash` | VARCHAR(255) | BCrypt strength 12 |
| `role` | VARCHAR(20) | ADMIN / VENDOR_ADMIN / VENDOR_USER / CUSTOMER |
| `vendor_id` | UUID | FK → vendors, nullable |
| `status` | VARCHAR(20) | ACTIVE / SUSPENDED / PASSWORD_RESET_REQUIRED |
| `account_number` | VARCHAR(64) | nullable, CUSTOMER only |
| `phone` | VARCHAR(32) | nullable |
| `must_change_password` | BOOLEAN | **new** — default FALSE |
| `created_at` | TIMESTAMPTZ | |
| `updated_at` | TIMESTAMPTZ | |

### 4.3 New `identity.refresh_tokens` Schema

| Column | Type | Notes |
|--------|------|-------|
| `id` | UUID | PK |
| `user_id` | UUID | FK → users, CASCADE DELETE |
| `token_hash` | VARCHAR(64) | SHA-256 hex of raw token |
| `issued_at` | TIMESTAMPTZ | |
| `expires_at` | TIMESTAMPTZ | |
| `revoked_at` | TIMESTAMPTZ | NULL = active |
| `replaced_by_token_id` | UUID | self-referential FK, nullable |
| `user_agent` | VARCHAR(512) | nullable |
| `ip_address` | VARCHAR(45) | nullable |

---

## 5. Security Design

### 5.1 RBAC Enforcement

**Layer 1 — Coarse-grained**: `@PreAuthorize("hasRole('ADMIN')")` on controller methods.
Role extracted from JWT `roles` claim by existing `PlatformJwtAuthenticationConverter`.

**Layer 2 — Fine-grained**: Application service uses `SecurityUtils.getCurrentVendorId()` to enforce
vendor isolation (VENDOR_ADMIN can only list/manage users within own vendorId).

### 5.2 Password Reset Security

- Temporary password: 10-char SecureRandom (upper + lower + digit + special chars)
- Returned once in response body — **never logged** (enforced by coding standards)
- `mustChangePassword = true` blocks normal operations; flag returned in `LoginResponse`
- Frontend responsible for redirecting to password-change flow

### 5.3 Refresh Token Security

- Raw token: 32-byte `SecureRandom` → URL-safe Base64 (same as `generateSecureToken()`)
- Stored as SHA-256 hex digest (64 chars) only
- Token in response body `data.refreshToken` (MVP — no httpOnly cookie for simplicity)
- **Rotation**: every `/auth/refresh` call issues new token, revokes old
- **Reuse detection**: revoked token presented → revoke ALL user tokens → 401
- **Expiry**: 30 days (`app.jwt.refresh-token-expiry-days=30`)
- **Max active tokens**: 5 per user (oldest revoked on new issue)

### 5.4 IdentitySecurityConfig Changes

```java
// Public endpoints — add:
"/api/v1/identity/auth/refresh"

// Authenticated (any role) — add:
"/api/v1/identity/users/me/password"
"/api/v1/identity/auth/logout"

// ADMIN or VENDOR_ADMIN — via @PreAuthorize on controller method:
"/api/v1/identity/users"                        // GET
"/api/v1/identity/users/*/password/reset"       // POST
```

---

## 6. Application Services Design

### CredentialManagementApplicationService

```
resetPassword(UUID targetUserId, UUID adminId) → PasswordResetResponse
  1. Load target User by id (404 if not found)
  2. Generate 10-char temp password via SecureRandom
  3. BCrypt encode temp password
  4. user.initiatePasswordReset(tempHash) → sets mustChangePassword=true
  5. eventPublisher.publishEvent(PasswordResetInitiatedEvent)
  6. userRepository.save(user)
  7. Return PasswordResetResponse with raw tempPassword (one-time)

changePassword(UUID userId, String currentPassword, String newPassword) → void
  1. Load User by id (404 if not found)
  2. Verify newPassword complexity (min 8, mixed case, digit, special)
  3. Verify currentPassword matches stored hash
  4. user.changePassword(newHash)
  5. eventPublisher.publishEvent(PasswordChangedEvent)
  6. userRepository.save(user)
```

### RefreshTokenApplicationService

```
refresh(String rawToken, String userAgent, String ip) → LoginResponse
  1. SHA-256 hash rawToken
  2. Load RefreshToken by tokenHash (401 if not found)
  3. If token.revokedAt != null → revokeAll + raise ReuseDetectedEvent → 401
  4. If token.expiresAt < now → 401
  5. Load User (401 if suspended)
  6. Create new RefreshToken, mark old as rotated
  7. Issue new JWT via tokenIssuerPort.issue(user)
  8. Return LoginResponse { accessToken, refreshToken: newRawToken, mustChangePassword }

logout(UUID userId) → void
  1. refreshTokenRepository.revokeAllByUserId(userId)
```

### AuthenticationApplicationService (modified)

Login flow extended: after successful credential validation, call
`refreshTokenApplicationService.issueOnLogin(userId, userAgent, ip)` to issue a refresh token.
Include `refreshToken` and `mustChangePassword` in `LoginResponse`.

---

## 7. Infrastructure Design

### UserDbEntity (modified)

Add: `@Column(name = "must_change_password") private boolean mustChangePassword`
Update mapper: `reconstitute()` and `toDbEntity()` include new field.

### RefreshTokenDbEntity (new)

```java
@Entity @Table(name = "refresh_tokens", schema = "identity")
class RefreshTokenDbEntity {
  @Id UUID id;
  @Column(name = "user_id") UUID userId;
  @Column(name = "token_hash") String tokenHash;
  @Column(name = "issued_at") Instant issuedAt;
  @Column(name = "expires_at") Instant expiresAt;
  @Column(name = "revoked_at") Instant revokedAt;          // nullable
  @Column(name = "replaced_by_token_id") UUID replacedByTokenId;  // nullable
  @Column(name = "user_agent") String userAgent;
  @Column(name = "ip_address") String ipAddress;
}
```

### RefreshTokenJpaRepository (new Spring Data)

```java
interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenDbEntity, UUID> {
  Optional<RefreshTokenDbEntity> findByTokenHash(String tokenHash);
  List<RefreshTokenDbEntity> findByUserIdAndRevokedAtIsNull(UUID userId);
  int countByUserIdAndRevokedAtIsNull(UUID userId);

  @Modifying @Query("UPDATE RefreshTokenDbEntity r SET r.revokedAt = :now " +
                    "WHERE r.userId = :userId AND r.revokedAt IS NULL")
  int revokeAllActiveByUserId(UUID userId, Instant now);
}
```

---

## 8. DTO Design

### New DTOs

```java
// API response for password reset
record PasswordResetResponse(UUID userId, String temporaryPassword, boolean mustChangePassword, String message)

// API request for password change
record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min=8) String newPassword)

// GET /users collection item
record UserSummaryResponse(UUID id, String name, String email, String role,
                           UUID vendorId, String status, boolean mustChangePassword, Instant createdAt)
```

### Modified DTOs

**`LoginResponse`** — add two fields:
```java
record LoginResponse(
  String accessToken, long expiresIn, UUID userId, String role, UUID vendorId,
  String refreshToken,       // new — opaque raw refresh token
  boolean mustChangePassword // new — frontend shows change-password prompt
)
```

---

## 9. NFR Implementation

| Requirement | Design |
|-------------|--------|
| No sensitive data in logs | `temporaryPassword` and `rawToken` excluded from all log calls |
| Performance — token lookup | `token_hash` unique index; O(1) lookup |
| Stateless JWT maintained | Access tokens unchanged; only refresh tokens are stateful |
| Audit trail | Domain events published for password reset, reuse detection |
| Token theft mitigation | Full family revocation on reuse detection |
| Schema cleanup readiness | `expires_at` column enables future scheduled cleanup job |

---

## 10. Completion Criteria

- [x] Architecture pattern selected (Hexagonal, consistent with existing module)
- [x] All layers designed with responsibilities
- [x] API contracts defined (5 new endpoints)
- [x] Database schema designed (V7 migration)
- [x] Security patterns applied (dual-layer RBAC, token hashing, reuse detection, no-log policy)
- [x] NFRs addressed in design
