---
stage: model
bolt: 003-identity-service-3
created: "2026-07-25T07:24:00Z"
---

# Static Model: 003-identity-service-3 — RBAC & Credential Management

## Context

This bolt completes the Identity & Access Service by adding:

1. **RBAC enforcement** — fine-grained permission model layered on top of the existing role enum
2. **Credential management** — admin-initiated password reset with forced change on next login
3. **Refresh token rotation** — stateful `RefreshToken` entity with reuse detection (rolling tokens)

All three features extend or evolve the **User** aggregate and the authentication infrastructure
already built in bolts 001 and 002. No new aggregate roots are introduced beyond `RefreshToken`.

---

## 1. Entities

### User (Aggregate Root — Modified)

- **Identity**: `UUID id`
- **Existing fields**: `name`, `email`, `passwordHash`, `phone`, `role`, `vendorId`, `accountNumber`, `status`, `createdAt`, `updatedAt`
- **New field**: `mustChangePassword: boolean` — explicit flag set by admin reset, cleared on change

**UserStatus extended** with: `PASSWORD_RESET_REQUIRED` (in addition to existing `ACTIVE`, `SUSPENDED`)

**Domain Invariants**:
1. A user with `mustChangePassword = true` can authenticate but MUST change password first.
2. Only `ACTIVE` or `PASSWORD_RESET_REQUIRED` users can log in — `SUSPENDED` users get 403.
3. Password reset is only performable by `ADMIN` role (enforced at application layer).
4. `mustChangePassword` is cleared to `false` on every successful `changePassword` call.

**New Behaviour Methods**:
- `initiatePasswordReset(String temporaryPasswordHash)` — sets `passwordHash` to temp value, sets `mustChangePassword = true`, updates `updatedAt`
- `changePassword(String newHash)` — sets new hash, clears `mustChangePassword`, updates `updatedAt`

---

### RefreshToken (Aggregate Root — New)

A stateful entity stored per user. Implements **refresh token rotation** with reuse detection.

- **Identity**: `UUID id`
- `userId: UUID` — owning user
- `tokenHash: String` — SHA-256 hash of the raw opaque token (never store raw token in DB)
- `issuedAt: Instant`
- `expiresAt: Instant`
- `revokedAt: Instant` (nullable — null means valid)
- `replacedByTokenId: UUID` (nullable — set on rotation, enables reuse detection chain)
- `userAgent: String` (nullable — audit/device tracking)
- `ipAddress: String` (nullable — audit)

**Domain Invariants**:
1. Valid only if `revokedAt == null AND expiresAt > now`.
2. On rotation: existing token gets `revokedAt = now`, `replacedByTokenId = newToken.id`.
3. Reuse detection: if a revoked token is presented → **revoke ALL tokens for that user** (token theft).
4. Refresh token TTL: 30 days (configurable via `app.jwt.refresh-token-expiry-days`).
5. Maximum 5 active tokens per user (oldest revoked when limit exceeded).

**Factory Method**:
- `RefreshToken.issue(userId, rawToken, expiresAt, userAgent, ipAddress)` — hashes the raw token, creates entity

**Behaviour Methods**:
- `rotate(UUID newTokenId)` — marks this token replaced, sets `revokedAt`
- `revoke()` — marks `revokedAt = now`
- `isValid(Instant now)` — returns `revokedAt == null && expiresAt.isAfter(now)`

---

## 2. Value Objects

### `Permission` (Enum — New)

Fine-grained permission codes derived from `Role` at runtime via `RolePermissionMapping`. **Not stored in DB for MVP.**

```
STATION_READ, STATION_WRITE, STATION_MANAGE,
SESSION_READ, SESSION_START, SESSION_STOP,
BILLING_READ, BILLING_MANAGE,
USER_READ, USER_MANAGE, CREDENTIAL_RESET,
VENDOR_READ, VENDOR_MANAGE
```

### `RolePermissionMapping` (Domain helper — New)

Static immutable mapping `Role → Set<Permission>`:

```
ADMIN         → ALL permissions
VENDOR_ADMIN  → STATION_READ, STATION_WRITE, STATION_MANAGE,
                SESSION_READ, SESSION_START, SESSION_STOP,
                BILLING_READ, BILLING_MANAGE,
                USER_READ, USER_MANAGE, VENDOR_READ
VENDOR_USER   → STATION_READ, SESSION_READ, SESSION_START, SESSION_STOP, BILLING_READ
CUSTOMER      → SESSION_READ, SESSION_START, SESSION_STOP, BILLING_READ
```

### `RawRefreshToken` (Value Object — New)

Wraps the cryptographically random opaque token string returned to the client. Never persisted.

- `value: String` — 32-byte URL-safe Base64 random string
- Factory: `RawRefreshToken.generate()`

---

## 3. Aggregates

### User Aggregate (Extended)

| Member | Change |
|--------|--------|
| `UserStatus` | Extend enum with `PASSWORD_RESET_REQUIRED` |
| `mustChangePassword: boolean` | **New field** |
| `initiatePasswordReset(String)` | **New method** |
| `changePassword(String)` | **New method** |

### RefreshToken Aggregate (New)

`RefreshToken` is its own aggregate root. Lifecycle is independent of `User` — tokens can be
invalidated, rotated, or expired without touching the `User` aggregate.

---

## 4. Domain Events

### `PasswordResetInitiatedEvent` (New)

| Field | Type | Description |
|-------|------|-------------|
| `targetUserId` | UUID | User whose password was reset |
| `initiatedByAdminId` | UUID | Admin performing the reset |
| `occurredOn` | Instant | Timestamp |

**Consumer**: Notification module sends email with temporary password instructions.

### `PasswordChangedEvent` (New)

| Field | Type | Description |
|-------|------|-------------|
| `userId` | UUID | User who changed password |
| `occurredOn` | Instant | Timestamp |

**Consumer**: Audit logging.

### `RefreshTokenIssuedEvent` (New)

| Field | Type |
|-------|------|
| `userId` | UUID |
| `tokenId` | UUID |
| `expiresAt` | Instant |
| `occurredOn` | Instant |

### `RefreshTokenRotatedEvent` (New)

| Field | Type |
|-------|------|
| `userId` | UUID |
| `oldTokenId` | UUID |
| `newTokenId` | UUID |
| `occurredOn` | Instant |

### `RefreshTokenReuseDetectedEvent` (New)

| Field | Type |
|-------|------|
| `userId` | UUID |
| `revokedCount` | int |
| `occurredOn` | Instant |

---

## 5. Domain Services

### `CredentialManagementDomainService` (New)

- `resetPassword(User target, UUID adminId, PasswordEncoder encoder)`
  → generates secure temp password, calls `target.initiatePasswordReset(hash)`,
    raises `PasswordResetInitiatedEvent`, returns raw temp password string

- `changePassword(User user, String currentPassword, String newPassword, PasswordEncoder encoder)`
  → verifies current hash matches, calls `user.changePassword(newHash)`,
    raises `PasswordChangedEvent`

### `RefreshTokenDomainService` (New)

- `issueRefreshToken(UUID userId, String userAgent, String ip)`
  → generates `RawRefreshToken`, creates `RefreshToken`, enforces 5-token limit

- `rotateRefreshToken(String rawToken, UUID userId)`
  → validates token, creates replacement, marks old as rotated, raises `RefreshTokenRotatedEvent`

- `detectAndHandleReuse(UUID userId)`
  → revokes ALL tokens, raises `RefreshTokenReuseDetectedEvent`

- `revokeAll(UUID userId)` → called on explicit logout

---

## 6. Repository Interfaces

### `UserRepository` (Extended)

```java
// Existing
User save(User user);
Optional<User> findById(UUID id);
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
boolean existsByAccountNumber(String accountNumber);

// New
List<User> findAllByVendorId(UUID vendorId);
List<User> findAllByRole(Role role);
```

### `RefreshTokenRepository` (New)

```java
RefreshToken save(RefreshToken token);
Optional<RefreshToken> findByTokenHash(String tokenHash);
List<RefreshToken> findAllActiveByUserId(UUID userId);
int countActiveByUserId(UUID userId);
void revokeAllByUserId(UUID userId);
```

---

## 7. Ubiquitous Language

| Term | Definition |
|------|------------|
| **Password Reset** | Admin-initiated action: sets temp password + `mustChangePassword = true` |
| **Forced Password Change** | State where user must change password before any other action |
| **Refresh Token Rotation** | Each use issues a new token and revokes the old one |
| **Reuse Detection** | Revoked token presented → token theft → revoke all user tokens |
| **RBAC** | Role-Based Access Control via `Role` enum |
| **Permission** | Fine-grained capability code (e.g. `STATION_WRITE`) derived from `Role` |
| **Token Hash** | SHA-256 hash of the raw opaque refresh token — stored in DB |
| **Raw Token** | Opaque random string returned to client — never persisted |
| **Active Token** | `RefreshToken` where `revokedAt = null AND expiresAt > now` |

---

## 8. Story Coverage

| Acceptance Criterion | Modelled By |
|---------------------|-------------|
| Admin resets user password → temp password generated | `User.initiatePasswordReset()`, `CredentialManagementDomainService.resetPassword()` |
| User with temp password → prompted to change | `mustChangePassword` flag on `User` |
| VENDOR_ADMIN → can manage vendor resources/users | `RolePermissionMapping`: VENDOR_ADMIN has `USER_MANAGE`, `STATION_MANAGE` |
| VENDOR_USER → limited access per permissions | `RolePermissionMapping`: VENDOR_USER is read+session only |
| API without required role → 403 Forbidden | `Permission` enum + Spring Security `@PreAuthorize` (Stage 4) |

---

## 9. Completion Criteria

- [x] All domain entities identified and documented
- [x] Business rules captured for each entity
- [x] Aggregate boundaries defined (User, RefreshToken)
- [x] Domain events specified (5 new events)
- [x] Repository interfaces defined (UserRepository extended, RefreshTokenRepository new)
- [x] All story acceptance criteria covered by domain model

