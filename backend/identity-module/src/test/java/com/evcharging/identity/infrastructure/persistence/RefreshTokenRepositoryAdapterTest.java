package com.evcharging.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.identity.domain.model.RefreshToken;

@DisplayName("RefreshTokenRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryAdapterTest {

  @Mock private RefreshTokenJpaRepository jpa;

  private RefreshTokenRepositoryAdapter adapter;

  private static final UUID USER_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    adapter = new RefreshTokenRepositoryAdapter(jpa);
  }

  private RefreshToken createToken() {
    return RefreshToken.issue(
        USER_UUID, "rawToken123", Instant.now().plusSeconds(3600), "Mozilla/5.0", "127.0.0.1");
  }

  private RefreshTokenDbEntity createEntity() {
    return RefreshTokenDbEntity.from(createToken(), true);
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves new token")
    void shouldSaveNewToken() {
      RefreshToken token = createToken();
      given(jpa.existsById(token.getId())).willReturn(false);
      given(jpa.save(any(RefreshTokenDbEntity.class))).willAnswer(inv -> inv.getArgument(0));

      RefreshToken result = adapter.save(token);

      assertThat(result).isNotNull();
      then(jpa).should().save(any(RefreshTokenDbEntity.class));
    }

    @Test
    @DisplayName("updates existing token")
    void shouldUpdateExistingToken() {
      RefreshToken token = createToken();
      given(jpa.existsById(token.getId())).willReturn(true);
      given(jpa.save(any(RefreshTokenDbEntity.class))).willAnswer(inv -> inv.getArgument(0));

      RefreshToken result = adapter.save(token);

      assertThat(result).isNotNull();
    }
  }

  @Nested
  @DisplayName("findByTokenHash")
  class FindByTokenHash {

    @Test
    @DisplayName("returns token when found")
    void shouldReturnToken() {
      given(jpa.findByTokenHash("hash123")).willReturn(Optional.of(createEntity()));

      Optional<RefreshToken> result = adapter.findByTokenHash("hash123");
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findByTokenHash("unknown")).willReturn(Optional.empty());

      assertThat(adapter.findByTokenHash("unknown")).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAllActiveByUserId")
  class FindAllActiveByUserId {

    @Test
    @DisplayName("returns active tokens")
    void shouldReturnActiveTokens() {
      given(jpa.findByUserIdAndRevokedAtIsNull(USER_UUID))
          .willReturn(List.of(createEntity()));

      List<RefreshToken> result = adapter.findAllActiveByUserId(USER_UUID);
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("countActiveByUserId")
  class CountActiveByUserId {

    @Test
    @DisplayName("returns count")
    void shouldReturnCount() {
      given(jpa.countByUserIdAndRevokedAtIsNull(USER_UUID)).willReturn(3);

      assertThat(adapter.countActiveByUserId(USER_UUID)).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("revokeAllByUserId")
  class RevokeAllByUserId {

    @Test
    @DisplayName("revokes all active tokens")
    void shouldRevokeAll() {
      adapter.revokeAllByUserId(USER_UUID);

      then(jpa).should().revokeAllActiveByUserId(eq(USER_UUID), any(Instant.class));
    }
  }
}
