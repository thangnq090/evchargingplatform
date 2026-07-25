---
unit: 001-identity-service
bolt: 001-identity-service-1
stage: model
status: complete
updated: "2026-07-24T20:17:17Z"
---

# Static Model - Identity & Access Service (Bolt 1)

## Bounded Context

The Identity & Access Bounded Context is responsible for managing all user identities, credentials, roles, permissions, and vendor organization profiles. It acts as the core authentication and authorization authority for the platform. For Bolt 1, the context focuses specifically on Platform Admin registration, Vendor creation, and Vendor user invitation/onboarding.

## Domain Entities

| Entity | Properties | Business Rules |
|--------|------------|----------------|
| **User** | `id`: UserId (UUID)<br>`name`: String<br>`email`: Email<br>`passwordHash`: HashedPassword<br>`role`: Role<br>`vendorId`: VendorId (optional)<br>`status`: UserStatus<br>`createdAt`: Instant<br>`updatedAt`: Instant | - Email must be unique across the platform.<br>- `vendorId` must be non-null if role is VENDOR_ADMIN or VENDOR_USER.<br>- `vendorId` must be null if role is ADMIN.<br>- Status transitions: PENDING_INVITATION -> ACTIVE, ACTIVE -> SUSPENDED, SUSPENDED -> ACTIVE. |
| **Vendor** | `id`: VendorId (UUID)<br>`name`: String<br>`status`: VendorStatus<br>`createdAt`: Instant<br>`updatedAt`: Instant | - Vendor name must be unique.<br>- If vendor is suspended, all associated users are effectively blocked from API operations. |
| **Invitation** | `id`: InvitationId (UUID)<br>`email`: Email<br>`vendorId`: VendorId<br>`role`: Role<br>`token`: InvitationToken<br>`status`: InvitationStatus<br>`createdAt`: Instant | - Token must be secure random and expire in 48 hours.<br>- Can only be accepted if status is PENDING and not expired. |

## Value Objects

| Value Object | Properties | Constraints |
|--------------|------------|-------------|
| **Email** | `value`: String | - Must conform to valid RFC 5322 format.<br>- Stored and compared as case-insensitive, lowercase. |
| **HashedPassword** | `value`: String | - Must be hashed using BCrypt/Argon2.<br>- Plain text passwords must never be stored or exposed in memory beyond validation. |
| **InvitationToken** | `value`: String<br>`expiresAt`: Instant | - Generated securely (e.g. 32-character hex/base64 string).<br>- `expiresAt` must be in the future when checked. |

## Aggregates

| Aggregate Root | Members | Invariants |
|----------------|---------|------------|
| **User** | User (Root) | - User must have exactly one role.<br>- Active user must have a non-blank name and valid email. |
| **Vendor** | Vendor (Root) | - Name cannot be blank.<br>- Cannot have a null status. |
| **Invitation** | Invitation (Root) | - Invitation belongs to exactly one Vendor and has one target Role. |

## Domain Events

| Event | Trigger | Payload |
|-------|---------|---------|
| **AdminRegisteredEvent** | Admin registers successfully. | `userId`: UUID, `email`: String, `name`: String, `timestamp`: Instant |
| **VendorCreatedEvent** | Platform Admin creates a vendor. | `vendorId`: UUID, `name`: String, `timestamp`: Instant |
| **VendorInvitationIssuedEvent** | Invitation to VENDOR_ADMIN is generated. | `invitationId`: UUID, `vendorId`: UUID, `email`: String, `token`: String, `expiresAt`: Instant |
| **VendorInvitationAcceptedEvent** | Invited user accepts and registers. | `invitationId`: UUID, `userId`: UUID, `email`: String, `vendorId`: UUID, `role`: String |
| **VendorUserCreatedEvent** | VENDOR_ADMIN adds a new VENDOR_USER. | `userId`: UUID, `vendorId`: UUID, `email`: String, `role`: String, `timestamp`: Instant |

## Domain Services

| Service | Operations | Dependencies |
|---------|------------|--------------|
| **UserRegistrationService** | - `registerAdmin(name, email, password)`: Registers a platform admin user.<br>- `createVendorWithAdmin(vendorName, adminName, adminEmail)`: Creates vendor and generates VENDOR_ADMIN invitation.<br>- `acceptInvitation(token, password)`: Onboards user as VENDOR_ADMIN after token validation.<br>- `addVendorUser(vendorAdminId, name, email, role)`: Creates additional VENDOR_USER for the same vendor. | `UserRepository`<br>`VendorRepository`<br>`InvitationRepository`<br>`PasswordEncoder` |

## Repository Interfaces

| Repository | Entity | Methods |
|------------|--------|---------|
| **UserRepository** | User | `save(User): User`<br>`findById(UserId): Optional<User>`<br>`findByEmail(Email): Optional<User>`<br>`existsByEmail(Email): boolean` |
| **VendorRepository** | Vendor | `save(Vendor): Vendor`<br>`findById(VendorId): Optional<Vendor>`<br>`existsByName(String): boolean` |
| **InvitationRepository** | Invitation | `save(Invitation): Invitation`<br>`findByToken(InvitationToken): Optional<Invitation>`<br>`findById(InvitationId): Optional<Invitation>` |

## Ubiquitous Language

| Term | Definition |
|------|------------|
| **Admin** | Platform-level administrator with authority over all vendors, stations, and users. |
| **Vendor** | An independent business entity providing charging services and owning/managing charging stations. |
| **Vendor Admin** | The primary user of a Vendor organization, empowered to invite other users and manage vendor-specific settings. |
| **Vendor User** | An operator or analyst user under a Vendor organization with read/write access limited to that vendor's scope. |
| **Invitation** | A system invitation sent to a prospective user, enabling them to register with a specific role under a vendor. |
| **Invitation Token** | A secure, single-use, time-bound token embedded in the invitation used to authenticate the registration flow. |
