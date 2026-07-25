package com.evcharging.identity.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link RefreshToken} domain aggregate. */
@DisplayName("RefreshToken")
class RefreshTokenTest {

  @Nested
  @DisplayName("issue()")
  class Issue {

    @Test
    @DisplayName("creates valid refresh token with all fields")
    void createsValidToken() {
      UUID userId = UUID.randomUUID();
      String rawToken = "test-raw-token-12345";
      Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
      String userAgent = "Mozilla/5.0";
      String ipAddress = "192.168.1.1";

      RefreshToken token = RefreshToken.issue(userId, rawToken, expiresAt, userAgent, ipAddress);

      assertNotNull(token.getId());
      assertEquals(userId, token.getUserId());
      assertNotNull(token.getTokenHash());
      assertNotNull(token.getIssuedAt());
      assertEquals(expiresAt, token.getExpiresAt());
      assertNull(token.getRevokedAt());
      assertNull(token.getReplacedByTokenId());
      assertEquals(userAgent, token.getUserAgent());
      assertEquals(ipAddress, token.getIpAddress());
    }

    @Test
    @DisplayName("generates consistent hash for same raw token")
    void generatesConsistentHash() {
      String rawToken = "test-raw-token-12345";

      String hash1 = RefreshToken.hashToken(rawToken);
      String hash2 = RefreshToken.hashToken(rawToken);

      assertEquals(hash1, hash2);
      assertEquals(64, hash1.length()); // SHA-256 produces 64 hex chars
    }
  }

  @Nested
  @DisplayName("isValid()")
  class IsValid {

    @Test
    @DisplayName("returns true for active non-expired token")
    void activeNonExpiredTokenIsValid() {
      RefreshToken token = createValidToken();

      assertTrue(token.isValid(Instant.now()));
    }

    @Test
    @DisplayName("returns false for revoked token")
    void revokedTokenIsInvalid() {
      RefreshToken token = createValidToken();
      token.revoke();

      assertFalse(token.isValid(Instant.now()));
    }

    @Test
    @DisplayName("returns false for expired token")
    void expiredTokenIsInvalid() {
      Instant pastExpiry = Instant.now().minus(1, ChronoUnit.HOURS);
      RefreshToken token =
          RefreshToken.issue(UUID.randomUUID(), "raw-token", pastExpiry, null, null);

      assertFalse(token.isValid(Instant.now()));
    }
  }

  @Nested
  @DisplayName("rotate()")
  class Rotate {

    @Test
    @DisplayName("marks token as revoked and sets replacement")
    void marksRevokedAndSetsReplacement() {
      RefreshToken token = createValidToken();
      UUID newTokenId = UUID.randomUUID();

      token.rotate(newTokenId);

      assertNotNull(token.getRevokedAt());
      assertEquals(newTokenId, token.getReplacedByTokenId());
    }
  }

  @Nested
  @DisplayName("revoke()")
  class Revoke {

    @Test
    @DisplayName("sets revokedAt timestamp")
    void setsRevokedAt() {
      RefreshToken token = createValidToken();

      token.revoke();

      assertNotNull(token.getRevokedAt());
    }
  }

  private RefreshToken createValidToken() {
    return RefreshToken.issue(
        UUID.randomUUID(),
        "test-raw-token",
        Instant.now().plus(30, ChronoUnit.DAYS),
        "test-user-agent",
        "192.168.1.1");
  }
}
