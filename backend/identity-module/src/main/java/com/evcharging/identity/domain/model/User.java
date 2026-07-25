package com.evcharging.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain aggregate root for a platform User.
 *
 * <p>Pure Java — no Spring or JPA annotations. All state changes go through behavior methods to
 * preserve invariants.
 */
public class User {

  private final UUID id;
  private String name;
  private final String email;
  private String passwordHash;
  private final Role role;
  private final UUID vendorId;
  private UserStatus status;
  private final Instant createdAt;
  private Instant updatedAt;

  private User(
      UUID id,
      String name,
      String email,
      String passwordHash,
      Role role,
      UUID vendorId,
      UserStatus status,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
    this.vendorId = vendorId;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Create a new ADMIN user.
   *
   * @param name display name
   * @param email unique email address (stored lowercase)
   * @param passwordHash BCrypt hash of the plain-text password
   */
  public static User createAdmin(String name, String email, String passwordHash) {
    Instant now = Instant.now();
    return new User(
        UUID.randomUUID(),
        name,
        email.toLowerCase(),
        passwordHash,
        Role.ADMIN,
        null,
        UserStatus.ACTIVE,
        now,
        now);
  }

  /**
   * Create a VENDOR_ADMIN or VENDOR_USER as part of accepting an invitation.
   *
   * @param name display name
   * @param email unique email address (stored lowercase)
   * @param passwordHash BCrypt hash of the plain-text password
   * @param role must be VENDOR_ADMIN or VENDOR_USER
   * @param vendorId the vendor this user belongs to
   */
  public static User createVendorUser(
      String name, String email, String passwordHash, Role role, UUID vendorId) {
    if (role == Role.ADMIN || role == Role.CUSTOMER) {
      throw new IllegalArgumentException(
          "createVendorUser requires VENDOR_ADMIN or VENDOR_USER role");
    }
    if (vendorId == null) {
      throw new IllegalArgumentException("Vendor users must have a vendorId");
    }
    Instant now = Instant.now();
    return new User(
        UUID.randomUUID(),
        name,
        email.toLowerCase(),
        passwordHash,
        role,
        vendorId,
        UserStatus.ACTIVE,
        now,
        now);
  }

  /**
   * Reconstitute a User from persistence.
   *
   * <p>For use by infrastructure adapters only.
   */
  public static User reconstitute(
      UUID id,
      String name,
      String email,
      String passwordHash,
      Role role,
      UUID vendorId,
      UserStatus status,
      Instant createdAt,
      Instant updatedAt) {
    return new User(id, name, email, passwordHash, role, vendorId, status, createdAt, updatedAt);
  }

  /** Suspend this user. Only permitted if currently ACTIVE. */
  public void suspend() {
    if (this.status != UserStatus.ACTIVE) {
      throw new IllegalStateException("Only ACTIVE users can be suspended");
    }
    this.status = UserStatus.SUSPENDED;
    this.updatedAt = Instant.now();
  }

  /** Reactivate a suspended user. */
  public void activate() {
    if (this.status != UserStatus.SUSPENDED) {
      throw new IllegalStateException("Only SUSPENDED users can be activated");
    }
    this.status = UserStatus.ACTIVE;
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Role getRole() {
    return role;
  }

  public UUID getVendorId() {
    return vendorId;
  }

  public UserStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
