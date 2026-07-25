package com.evcharging.identity.infrastructure.persistence;

import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.model.VendorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

/**
 * JPA mapping entity for the {@code identity.vendors} table. Not exposed beyond
 * infrastructure.
 */
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

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Transient
  private boolean isNew = true;

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
    entity.createdAt = vendor.getCreatedAt();
    entity.updatedAt = vendor.getUpdatedAt();
    entity.isNew = isNew;
    return entity;
  }

  Vendor toDomain() {
    return Vendor.reconstitute(id, name, status, createdAt, updatedAt);
  }
}
