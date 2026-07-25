# DDD Stage 1: Domain Model — Bolt 002-identity-service-2

## 1. Overview & Context

This stage models the domain entities, value objects, and domain events for **Customer Registration** and **HMAC-SHA256 JWT Authentication** within `001-identity-service`.

## 2. Aggregate & Entity Updates

### User Aggregate (Modified)

- **Entity**: `User`
- **Identity**: `UserId`
- **New Attribute**: `accountNumber: AccountNumber` (Nullable for non-customer roles, mandatory for `CUSTOMER` role).

#### Fields:
- `id`: `UserId`
- `name`: `String`
- `email`: `String`
- `passwordHash`: `String`
- `phone`: `String`
- `role`: `Role` (`ADMIN`, `VENDOR_ADMIN`, `VENDOR_USER`, `CUSTOMER`)
- `vendorId`: `UUID` (Nullable)
- `accountNumber`: `String` (Nullable, auto-generated for `CUSTOMER` role e.g., `ACC-A1B2C3D4`)
- `createdAt`: `Instant`
- `updatedAt`: `Instant`

#### Domain Invariants & Rules:
1. When `role` is `CUSTOMER`, `accountNumber` MUST NOT be null or blank.
2. `accountNumber` MUST follow the format `ACC-` followed by an 8-character uppercase alphanumeric string.
3. Every customer registration triggers a `CustomerRegisteredEvent`.

## 3. Value Objects

### `AccountNumber`
- Enforces format: `ACC-[A-Z0-9]{8}`.
- Factory method `AccountNumber.generate()` produces a random 8-character hex/alphanumeric code prefixed with `ACC-`.

## 4. Domain Events

### `CustomerRegisteredEvent`
- **Fields**:
  - `userId`: `UUID`
  - `name`: `String`
  - `email`: `String`
  - `accountNumber`: `String`
  - `occurredOn`: `Instant`

## 5. Domain Repository Ports

### `UserRepository` (Updated)
- `boolean existsByAccountNumber(String accountNumber)`
- `Optional<User> findByAccountNumber(String accountNumber)`
- `boolean existsByPhone(String phone)`

## 6. Authentication Domain Contracts

- JWT Authentication uses **HMAC-SHA256** symmetric key signature.
- Tokens issued for `CUSTOMER` role include claims: `sub` (email), `roles` (`["CUSTOMER"]`), `user_id`, `account_number`.
