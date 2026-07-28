package com.evcharging.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.identity.domain.model.RefreshToken;

@DisplayName("RefreshTokenDbEntity")
class RefreshTokenDbEntityTest {

  private RefreshToken createToken() {
    return RefreshToken.issue(
        UUID.randomUUID(), "rawToken123", Instant.now().plusSeconds(3600), "Mozilla/5.0", "127.0.0.1");
  }

  @Nested
  @DisplayName("from")
  class From {

    @Test
    @DisplayName("round-trips domain to entity and back")
    void shouldRoundTrip() {
      RefreshToken token = createToken();

      RefreshTokenDbEntity entity = RefreshTokenDbEntity.from(token, true);
      RefreshToken domain = entity.toDomain();

      assertThat(domain.getId()).isEqualTo(token.getId());
      assertThat(domain.getUserId()).isEqualTo(token.getUserId());
      assertThat(domain.getTokenHash()).isEqualTo(token.getTokenHash());
      assertThat(domain.getUserAgent()).isEqualTo("Mozilla/5.0");
      assertThat(domain.getIpAddress()).isEqualTo("127.0.0.1");
      assertThat(entity.isNew()).isTrue();
    }

    @Test
    @DisplayName("marks as not new")
    void shouldMarkAsNotNew() {
      RefreshTokenDbEntity entity = RefreshTokenDbEntity.from(createToken(), false);
      assertThat(entity.isNew()).isFalse();
    }
  }
}
