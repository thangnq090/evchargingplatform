package com.evcharging.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.springframework.data.domain.Persistable;

import com.evcharging.identity.domain.model.RefreshToken;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens", schema = "identity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class RefreshTokenDbEntity implements Persistable<UUID> {

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "issued_at", nullable = false, updatable = false)
  private Instant issuedAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "replaced_by_token_id")
  private UUID replacedByTokenId;

  @Column(name = "user_agent", length = 512)
  private String userAgent;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Transient private boolean isNew = true;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  static RefreshTokenDbEntity from(RefreshToken token, boolean isNew) {
    RefreshTokenDbEntity entity = new RefreshTokenDbEntity();
    entity.id = token.getId();
    entity.userId = token.getUserId();
    entity.tokenHash = token.getTokenHash();
    entity.issuedAt = token.getIssuedAt();
    entity.expiresAt = token.getExpiresAt();
    entity.revokedAt = token.getRevokedAt();
    entity.replacedByTokenId = token.getReplacedByTokenId();
    entity.userAgent = token.getUserAgent();
    entity.ipAddress = token.getIpAddress();
    entity.isNew = isNew;
    return entity;
  }

  RefreshToken toDomain() {
    return RefreshToken.reconstitute(
        id,
        userId,
        tokenHash,
        issuedAt,
        expiresAt,
        revokedAt,
        replacedByTokenId,
        userAgent,
        ipAddress);
  }
}
