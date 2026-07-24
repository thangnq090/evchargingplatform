---
unit: 001-identity-service
intent: 001-ev-charging-mvp
phase: inception
status: draft
created: "2026-07-24T15:00:00Z"
updated: "2026-07-24T15:00:00Z"
---

# Unit Brief: Identity & Access Service

## Purpose
Provide user registration, authentication, authorization, and role-based access control for all platform actors (Admin, Vendor, Customer). This is the foundational unit with no internal dependencies — all other units depend on it for identity.

## Scope

### In Scope
- Admin user registration and management
- Vendor user management (VENDOR_ADMIN, VENDOR_USER roles + permissions)
- Customer registration with account number auto-generation
- JWT-based authentication (RS256/ES256 signed tokens)
- Role-based authorization (ADMIN, VENDOR_ADMIN, VENDOR_USER, CUSTOMER)
- Refresh token rotation with reuse detection
- User credential management and password reset
- RBAC on all API endpoints
- Password hashing (BCrypt/Argon2)
- Audit logging of auth events

### Out of Scope
- Social login / OAuth providers (deferred)
- MFA / 2FA (deferred)
- External IdP integration (spring security JWT for MVP; Keycloak/Azure AD later)
- User-facing profile management beyond credentials
- Chargepoint or session management (handled by other units)

---

## Assigned Requirements

| FR | Requirement | Priority |
|----|-------------|----------|
| FR-1 | Admin User Registration | Must |
| FR-2 | Vendor User Management (VENDOR_ADMIN, VENDOR_USER) | Must |
| FR-3 | Customer Registration (name, email, account number, phone) | Must |
| FR-4 | Authentication & Authorization (JWT RS256/ES256, RBAC) | Must |

---

## Domain Concepts

### Key Entities
| Entity | Description | Attributes |
|--------|-------------|------------|
| User | Platform user | id, name, email, password_hash, phone, role, vendor_id, created_at, updated_at |
| Role | Authorization role | ADMIN, VENDOR_ADMIN, VENDOR_USER, CUSTOMER |
| Permission | Fine-grained action permission | resource, action, scope (e.g., station:write) |
| RefreshToken | JWT refresh token | id, user_id, token_hash, expires_at, revoked_at |
| UserSession | Active user session | id, user_id, access_token_jti, refresh_token_id, expires_at |

### Key Operations
| Operation | Description | Inputs | Outputs |
|-----------|-------------|--------|---------|
| Register Admin | Create admin user | name, email, password | User, JWT tokens |
| Register Vendor User | Create vendor user + invitation | name, email, vendor_id, role | User, invitation |
| Register Customer | Create customer account | name, email, phone | User (with account number), JWT tokens |
| Authenticate | Login with credentials | email, password | JWT access token, refresh token |
| Refresh Token | Rotate refresh token | refresh_token | New JWT access token, new refresh token |
| Reset Credentials | Admin resets user password | user_id | Temporary password |
| Validate Token | Gateway validates JWT | JWT | Claims (roles, vendor_id, sub, exp) |

---

## Story Summary

| Metric | Count |
|--------|-------|
| Total Stories | ~5 |
| Must Have | 4 |
| Should Have | 1 |
| Could Have | 0 |

---

## Dependencies

### Depends On
| Unit | Reason |
|------|--------|
| None | Foundational unit |

### Depended By
| Unit | Reason |
|------|--------|
| All other units | Depend on identity for auth and user context |

### External Dependencies
| System | Purpose | Risk |
|--------|---------|------|
| PostgreSQL (identity schema) | User and role storage | Low |
| None for MVP | Auth handled in-process via Spring Security | Medium — abstract IdP interface ready for Keycloak/Azure AD later |

---

## Technical Context

### Suggested Technology
| Component | Technology |
|-----------|------------|
| Authentication Framework | Spring Security + JWT (RS256) |
| Token Library | Nimbus JOSE + JWT (Spring Security default for OAuth2 Resource Server) |
| Password Hashing | BCrypt (Spring Security `BCryptPasswordEncoder`) |
| DB Access | Spring Data JPA + Hibernate |
| Schema | `identity` schema in PostgreSQL |
| API | REST controllers under `/api/v1/auth/`, `/api/v1/users/` |

### Integration Points
| Integration | Type | Protocol |
|-------------|------|----------|
| Gateway JWT validation | In-process (Spring Security filter) | JWKS cache |
| User lookup | Internal API (Spring service) | Java interface |
| Credential reset | Internal API | Domain event `UserCredentialsResetEvent` |

### Data Storage
| Data | Type | Volume | Retention |
|------|------|--------|-----------|
| Users | SQL (identity schema) | 100K+ rows | Indefinite (soft-delete) |
| Refresh tokens | SQL | Expired tokens cleaned | 90 days |
| Sessions | SQL | Active only | Until logout/expiry |

---

## Constraints

- JWT signing algorithm: RS256 (asymmetric) — avoid HS256 for distributed validation
- Auth Service signs with private key; Gateway + Resource Servers validate with public key
- JWT claims must include: sub, iat, exp, iss, aud, roles, vendor_id (for VENDOR roles)
- Tokens rejected for: invalid signature, expired timestamp, invalid issuer, invalid audience, missing required claims
- All passwords hashed with BCrypt (never stored in plaintext)
- Account numbers for customers are auto-generated (UUID or sequential prefixed)

---

## Success Criteria

### Functional
- [ ] Admin can register and log in
- [ ] Vendor users can be created with VENDOR_ADMIN or VENDOR_USER role
- [ ] Customers can register with auto-generated account number
- [ ] JWT tokens signed with RS256, validated by Gateway
- [ ] RBAC enforced on all API endpoints
- [ ] Refresh token rotation works correctly

### Non-Functional
- [ ] Password hashing prevents credential exposure
- [ ] Token expiry enforced (short-lived access tokens: 15 min)
- [ ] IdP interface abstracted for future Keycloak/Azure AD integration

### Quality
- [ ] Test coverage > 80%
- [ ] Auth integration tests (Spring Security test)
- [ ] No plaintext passwords in logs or responses

---

## Bolt Suggestions

| Bolt | Type | Stories | Objective |
|------|------|---------|-----------|
| bolt-001-identity-1 | DDD | S1, S2, S3 | User registration and auth (Admin, Vendor, Customer) |
| bolt-001-identity-2 | DDD | S4, S5 | RBAC, permissions, credential management |

---

## Notes

**Critical dependency**: All other units depend on this unit being operational. Identity must be deployed first. The abstract IdP interface (`IdentityProvider` port) should be designed early to avoid coupling to Spring Security internals — allows swap to Keycloak/Azure AD without domain changes.
