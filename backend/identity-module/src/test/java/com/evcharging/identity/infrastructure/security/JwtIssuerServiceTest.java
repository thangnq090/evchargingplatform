package com.evcharging.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.domain.model.Role;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

@DisplayName("JwtIssuerService")
class JwtIssuerServiceTest {

  private JwtIssuerService service;

  private static final String SECRET =
      java.util.Base64.getEncoder()
          .encodeToString("test-secret-key-that-is-long-enough-for-hs256-algorithm!12345".getBytes());

  @BeforeEach
  void setUp() {
    service = new JwtIssuerService(SECRET, 900000);
  }

  private User createUser(Role role, UUID vendorId) {
    return User.reconstitute(
        UUID.randomUUID(), "Test User", "test@test.com", "$2a$hash", null, role,
        vendorId, null, UserStatus.ACTIVE, false, Instant.now(), Instant.now());
  }

  @Nested
  @DisplayName("issue")
  class Issue {

    @Test
    @DisplayName("issues valid JWT for customer")
    void shouldIssueJwtForCustomer() {
      User user = createUser(Role.CUSTOMER, null);

      LoginResponse response = service.issue(user);

      assertThat(response.accessToken()).isNotBlank();
      assertThat(response.expiresIn()).isEqualTo(900);
      assertThat(response.userId()).isEqualTo(user.getId());
      assertThat(response.role()).isEqualTo("CUSTOMER");
      assertThat(response.vendorId()).isNull();
      assertThat(response.refreshToken()).isNull();
      assertThat(response.mustChangePassword()).isFalse();
    }

    @Test
    @DisplayName("issues JWT with vendor_id claim for vendor user")
    void shouldIncludeVendorId() {
      UUID vendorId = UUID.randomUUID();
      User user = createUser(Role.VENDOR_ADMIN, vendorId);

      LoginResponse response = service.issue(user);

      Jws<Claims> parsed =
          Jwts.parser().verifyWith(service.signingKey()).build().parseSignedClaims(response.accessToken());
      assertThat(parsed.getPayload().get("vendor_id")).isEqualTo(vendorId.toString());
    }

    @Test
    @DisplayName("issues JWT for admin")
    void shouldIssueJwtForAdmin() {
      User user = createUser(Role.ADMIN, null);

      LoginResponse response = service.issue(user);

      Jws<Claims> parsed =
          Jwts.parser().verifyWith(service.signingKey()).build().parseSignedClaims(response.accessToken());
      Claims claims = parsed.getPayload();
      assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
      assertThat(claims.get("role")).isEqualTo("ROLE_ADMIN");
      assertThat(claims.get("roles")).isEqualTo(List.of("ROLE_ADMIN"));
      assertThat(claims.get("realm_access")).isNotNull();
    }

    @Test
    @DisplayName("includes email claim")
    void shouldIncludeEmailClaim() {
      User user = createUser(Role.CUSTOMER, null);

      LoginResponse response = service.issue(user);

      Jws<Claims> parsed =
          Jwts.parser().verifyWith(service.signingKey()).build().parseSignedClaims(response.accessToken());
      assertThat(parsed.getPayload().get("email")).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("token has expiration in the future")
    void shouldHaveFutureExpiration() {
      User user = createUser(Role.CUSTOMER, null);

      LoginResponse response = service.issue(user);

      Jws<Claims> parsed =
          Jwts.parser().verifyWith(service.signingKey()).build().parseSignedClaims(response.accessToken());
      assertThat(parsed.getPayload().getExpiration()).isAfter(Instant.now());
    }
  }

  @Nested
  @DisplayName("signingKey")
  class SigningKey {

    @Test
    @DisplayName("generates consistent signing key")
    void shouldGenerateConsistentKey() {
      assertThat(service.signingKey()).isNotNull();
      assertThat(service.signingKey()).isEqualTo(service.signingKey());
    }
  }
}
