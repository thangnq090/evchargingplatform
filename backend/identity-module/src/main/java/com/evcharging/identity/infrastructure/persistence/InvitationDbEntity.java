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
import org.springframework.data.domain.Persistable;

import com.evcharging.identity.domain.model.Invitation;
import com.evcharging.identity.domain.model.InvitationStatus;
import com.evcharging.identity.domain.model.Role;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA mapping entity for the {@code identity.invitations} table. Not exposed beyond infrastructure.
 */
@Entity
@Table(name = "invitations", schema = "identity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class InvitationDbEntity implements Persistable<UUID> {

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private String email;

  @Column(name = "vendor_id", nullable = false)
  private UUID vendorId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InvitationStatus status;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Transient private boolean isNew = true;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  static InvitationDbEntity from(Invitation invitation, boolean isNew) {
    InvitationDbEntity entity = new InvitationDbEntity();
    entity.id = invitation.getId();
    entity.email = invitation.getEmail();
    entity.vendorId = invitation.getVendorId();
    entity.role = invitation.getRole();
    entity.token = invitation.getToken();
    entity.expiresAt = invitation.getExpiresAt();
    entity.status = invitation.getStatus();
    entity.createdAt = invitation.getCreatedAt();
    entity.isNew = isNew;
    return entity;
  }

  Invitation toDomain() {
    return Invitation.reconstitute(id, email, vendorId, role, token, expiresAt, status, createdAt);
  }
}
