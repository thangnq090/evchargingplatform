package com.evcharging.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.evcharging.identity.domain.model.RefreshToken;
import com.evcharging.identity.domain.repository.RefreshTokenRepository;

@Repository
class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

  private final RefreshTokenJpaRepository jpa;

  RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public RefreshToken save(RefreshToken token) {
    boolean isNew = !jpa.existsById(token.getId());
    RefreshTokenDbEntity entity = RefreshTokenDbEntity.from(token, isNew);
    RefreshTokenDbEntity saved = jpa.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return jpa.findByTokenHash(tokenHash).map(RefreshTokenDbEntity::toDomain);
  }

  @Override
  public List<RefreshToken> findAllActiveByUserId(UUID userId) {
    return jpa.findByUserIdAndRevokedAtIsNull(userId).stream()
        .map(RefreshTokenDbEntity::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public int countActiveByUserId(UUID userId) {
    return jpa.countByUserIdAndRevokedAtIsNull(userId);
  }

  @Override
  public void revokeAllByUserId(UUID userId) {
    jpa.revokeAllActiveByUserId(userId, Instant.now());
  }
}
