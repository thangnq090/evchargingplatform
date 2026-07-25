package com.evcharging.identity.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain aggregate root for a Vendor organization.
 *
 * <p>Pure Java — no Spring or JPA annotations.
 */
public class Vendor {

  private final UUID id;
  private final String name;
  private VendorStatus status;
  private final Instant createdAt;
  private Instant updatedAt;

  private Vendor(UUID id, String name, VendorStatus status, Instant createdAt, Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  /**
   * Create a new active Vendor.
   *
   * @param name unique vendor display name
   */
  public static Vendor create(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Vendor name must not be blank");
    }
    Instant now = Instant.now();
    return new Vendor(UUID.randomUUID(), name, VendorStatus.ACTIVE, now, now);
  }

  /**
   * Reconstitute a Vendor from persistence.
   *
   * <p>For use by infrastructure adapters only.
   */
  public static Vendor reconstitute(
      UUID id, String name, VendorStatus status, Instant createdAt, Instant updatedAt) {
    return new Vendor(id, name, status, createdAt, updatedAt);
  }

  /** Suspend the vendor. Only allowed if currently ACTIVE. */
  public void suspend() {
    if (this.status != VendorStatus.ACTIVE) {
      throw new IllegalStateException("Only ACTIVE vendors can be suspended");
    }
    this.status = VendorStatus.SUSPENDED;
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public VendorStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
