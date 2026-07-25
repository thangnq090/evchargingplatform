package com.evcharging.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenDbEntity, UUID> {

  Optional<RefreshTokenDbEntity> findByTokenHash(String tokenHash);

  List<RefreshTokenDbEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

  int countByUserIdAndRevokedAtIsNull(UUID userId);

  @Modifying
  @Query(
      "UPDATE RefreshTokenDbEntity r SET r.revokedAt = :now WHERE r.userId = :userId AND r.revokedAt IS NULL")
  int revokeAllActiveByUserId(UUID userId, Instant now);
}
