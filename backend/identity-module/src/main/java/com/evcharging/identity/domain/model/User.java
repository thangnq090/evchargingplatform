package com.evcharging.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
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
  private final String phone;
  private final Role role;
  private final UUID vendorId;
  private final String accountNumber;
  private UserStatus status;
  private boolean mustChangePassword;
  private final Instant createdAt;
  private Instant updatedAt;

  private User(
      UUID id,
      String name,
      String email,
      String passwordHash,
      String phone,
      Role role,
      UUID vendorId,
      String accountNumber,
      UserStatus status,
      boolean mustChangePassword,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.passwordHash = passwordHash;
    this.phone = phone;
    this.role = role;
    this.vendorId = vendorId;
    this.accountNumber = accountNumber;
    this.status = status;
    this.mustChangePassword = mustChangePassword;
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
        null,
        Role.ADMIN,
        null,
        null,
        UserStatus.ACTIVE,
        false,
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
        null,
        role,
        vendorId,
        null,
        UserStatus.ACTIVE,
        false,
        now,
        now);
  }

  /**
   * Create a CUSTOMER user.
   *
   * @param name display name
   * @param email unique email address (stored lowercase)
   * @param passwordHash BCrypt hash of the plain-text password
   * @param phone phone number
   * @param accountNumber auto-generated unique account number
   */
  public static User createCustomer(
      String name, String email, String passwordHash, String phone, String accountNumber) {
    Objects.requireNonNull(accountNumber, "Customer account number is required");
    Instant now = Instant.now();
    return new User(
        UUID.randomUUID(),
        name,
        email.toLowerCase(),
        passwordHash,
        phone,
        Role.CUSTOMER,
        null,
        accountNumber,
        UserStatus.ACTIVE,
        false,
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
    return reconstitute(
        id,
        name,
        email,
        passwordHash,
        null,
        role,
        vendorId,
        null,
        status,
        false,
        createdAt,
        updatedAt);
  }

  /** Reconstitute a User with phone and account number from persistence. */
  public static User reconstitute(
      UUID id,
      String name,
      String email,
      String passwordHash,
      String phone,
      Role role,
      UUID vendorId,
      String accountNumber,
      UserStatus status,
      boolean mustChangePassword,
      Instant createdAt,
      Instant updatedAt) {
    return new User(
        id,
        name,
        email,
        passwordHash,
        phone,
        role,
        vendorId,
        accountNumber,
        status,
        mustChangePassword,
        createdAt,
        updatedAt);
  }

  /** Reconstitute a User with phone and account number from persistence. (Legacy) */
  public static User reconstitute(
      UUID id,
      String name,
      String email,
      String passwordHash,
      String phone,
      Role role,
      UUID vendorId,
      String accountNumber,
      UserStatus status,
      Instant createdAt,
      Instant updatedAt) {
    return reconstitute(
        id,
        name,
        email,
        passwordHash,
        phone,
        role,
        vendorId,
        accountNumber,
        status,
        false,
        createdAt,
        updatedAt);
  }

  /** Suspend this user. Only permitted if currently ACTIVE. */
  public void suspend() {
    if (this.status != UserStatus.ACTIVE) {
      throw new IllegalStateException("Only ACTIVE users can be suspended");
    }
    this.status = UserStatus.SUSPENDED;
    this.updatedAt = Instant.now();
  }

  /**
   * Initiate password reset.
   *
   * @param temporaryPasswordHash the BCrypt hash of the temporary password
   */
  public void initiatePasswordReset(String temporaryPasswordHash) {
    this.passwordHash = temporaryPasswordHash;
    this.mustChangePassword = true;
    this.status = UserStatus.PASSWORD_RESET_REQUIRED;
    this.updatedAt = Instant.now();
  }

  /**
   * Change password.
   *
   * @param newPasswordHash the BCrypt hash of the new password
   */
  public void changePassword(String newPasswordHash) {
    this.passwordHash = newPasswordHash;
    this.mustChangePassword = false;
    if (this.status == UserStatus.PASSWORD_RESET_REQUIRED) {
      this.status = UserStatus.ACTIVE;
    }
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

  public String getPhone() {
    return phone;
  }

  public Role getRole() {
    return role;
  }

  public UUID getVendorId() {
    return vendorId;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public UserStatus getStatus() {
    return status;
  }

  public boolean isMustChangePassword() {
    return mustChangePassword;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
