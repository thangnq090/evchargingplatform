# DDD Stage 2: Technical Design — Bolt 002-identity-service-2

## 1. Overview

This document specifies the technical design, API signatures, database migrations, DTOs, and component updates for customer registration and customer authentication in `identity-module`.

## 2. Database Schema Migration

### Migration File: `V5__add_customer_account_number.sql`
Location: `backend/identity-module/src/main/resources/db/migration/identity-module/`

```sql
ALTER TABLE identity.users ADD COLUMN IF NOT EXISTS phone VARCHAR(32);
ALTER TABLE identity.users ADD COLUMN IF NOT EXISTS account_number VARCHAR(64);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_account_number ON identity.users(account_number) WHERE account_number IS NOT NULL;
```

## 3. Data Transfer Objects (DTOs)

### Request: `RegisterCustomerRequest`
```java
public record RegisterCustomerRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 100) String password,
    @NotBlank String phone
) {}
```

### Response: `CustomerRegistrationResponse`
```java
public record CustomerRegistrationResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String accountNumber,
    Role role,
    Instant createdAt
) {}
```

## 4. Controller API Contract

### Endpoint: Customer Self-Registration
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/customers/register`
- **Access**: Public
- **Request Body**: `RegisterCustomerRequest`
- **Response**: `ApiResponse<CustomerRegistrationResponse>` (HTTP Status 201 Created)
- **Error Codes**:
  - `409 Conflict` if email or phone is already registered.
  - `400 Bad Request` if validation fails.

## 5. Application Service Logic

### `UserRegistrationApplicationService.registerCustomer`
1. Check `userRepository.existsByEmail(request.email())`. If true, throw `UserAlreadyExistsException`.
2. Generate unique `accountNumber` via `AccountNumber.generate()`. Ensure non-collision via `userRepository.existsByAccountNumber(...)`.
3. Hash password using `passwordEncoder.encode(request.password())`.
4. Construct domain aggregate `User.registerCustomer(...)`.
5. Save `User` via `userRepository.save(user)`.
6. Return mapping to `CustomerRegistrationResponse`.

## 6. JWT Issuer Claim Integration

In `JwtIssuerService.generateToken(User user)`:
- Add claim `account_number` to JWT token if `user.getAccountNumber()` is present.
- Header algorithm: `HS256` (HMAC-SHA256).
