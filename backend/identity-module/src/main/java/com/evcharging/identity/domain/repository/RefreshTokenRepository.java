package com.evcharging.identity.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evcharging.identity.domain.model.RefreshToken;

/** Domain port for RefreshToken persistence operations. */
public interface RefreshTokenRepository {

  RefreshToken save(RefreshToken token);

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findAllActiveByUserId(UUID userId);

  int countActiveByUserId(UUID userId);

  void revokeAllByUserId(UUID userId);
}
