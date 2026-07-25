package com.evcharging.identity.infrastructure.persistence;

import com.evcharging.identity.domain.model.Role;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;
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
 * JPA mapping entity for the {@code identity.users} table. Not exposed beyond
 * infrastructure.
 */
@Entity
@Table(name = "users", schema = "identity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserDbEntity implements Persistable<UUID> {

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(name = "vendor_id")
  private UUID vendorId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status;

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

  /** Factory — create from domain aggregate. */
  static UserDbEntity from(User user, boolean isNew) {
    UserDbEntity entity = new UserDbEntity();
    entity.id = user.getId();
    entity.name = user.getName();
    entity.email = user.getEmail();
    entity.passwordHash = user.getPasswordHash();
    entity.role = user.getRole();
    entity.vendorId = user.getVendorId();
    entity.status = user.getStatus();
    entity.createdAt = user.getCreatedAt();
    entity.updatedAt = user.getUpdatedAt();
    entity.isNew = isNew;
    return entity;
  }

  /** Map back to domain aggregate. */
  User toDomain() {
    return User.reconstitute(
        id, name, email, passwordHash, role, vendorId, status, createdAt, updatedAt);
  }
}
