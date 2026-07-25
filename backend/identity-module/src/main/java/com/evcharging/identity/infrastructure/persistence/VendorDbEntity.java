package com.evcharging.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.model.VendorStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** JPA mapping entity for the {@code identity.vendors} table. Not exposed beyond infrastructure. */
@Entity
@Table(name = "vendors", schema = "identity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class VendorDbEntity implements Persistable<UUID> {

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VendorStatus status;

  /** Platform markup in basis points (1 BP = 0.01%). Defaults to 0. */
  @Column(name = "markup_basis_points", nullable = false)
  private int markupBasisPoints = 0;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Transient private boolean isNew = true;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  static VendorDbEntity from(Vendor vendor, boolean isNew) {
    VendorDbEntity entity = new VendorDbEntity();
    entity.id = vendor.getId();
    entity.name = vendor.getName();
    entity.status = vendor.getStatus();
    entity.markupBasisPoints = vendor.getMarkupPercentage().getBasisPoints();
    entity.createdAt = vendor.getCreatedAt();
    entity.updatedAt = vendor.getUpdatedAt();
    entity.isNew = isNew;
    return entity;
  }

  Vendor toDomain() {
    return Vendor.reconstitute(
        id,
        name,
        status,
        com.evcharging.shared.kernel.MarkupPercentage.of(markupBasisPoints),
        createdAt,
        updatedAt);
  }
}
