package com.evcharging.identity.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.springframework.security.authentication.BadCredentialsException;

import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.application.port.out.TokenIssuerPort;
import com.evcharging.identity.domain.model.RefreshToken;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.repository.RefreshTokenRepository;
import com.evcharging.identity.domain.repository.UserRepository;

/** Unit tests for {@link RefreshTokenApplicationService}. */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenApplicationService")
class RefreshTokenApplicationServiceTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private UserRepository userRepository;

  @Mock private TokenIssuerPort tokenIssuerPort;

  private RefreshTokenApplicationService service;

  @BeforeEach
  void setUp() {
    service =
        new RefreshTokenApplicationService(
            refreshTokenRepository, userRepository, tokenIssuerPort, 30);
  }

  @Nested
  @DisplayName("issueOnLogin()")
  class IssueOnLogin {

    @Test
    @DisplayName("issues new refresh token")
    void issuesNewToken() {
      UUID userId = UUID.randomUUID();
      String userAgent = "Mozilla/5.0";
      String ipAddress = "192.168.1.1";

      when(refreshTokenRepository.findAllActiveByUserId(userId)).thenReturn(List.of());
      when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

      String rawToken = service.issueOnLogin(userId, userAgent, ipAddress);

      assertNotNull(rawToken);
      assertTrue(rawToken.length() > 0);

      verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("revokes oldest token when limit reached")
    void revokesOldestWhenLimitReached() {
      UUID userId = UUID.randomUUID();
      RefreshToken oldToken1 = createToken(userId, Instant.now().minus(10, ChronoUnit.DAYS));
      RefreshToken oldToken2 = createToken(userId, Instant.now().minus(5, ChronoUnit.DAYS));
      RefreshToken oldToken3 = createToken(userId, Instant.now().minus(3, ChronoUnit.DAYS));
      RefreshToken oldToken4 = createToken(userId, Instant.now().minus(2, ChronoUnit.DAYS));
      RefreshToken oldToken5 = createToken(userId, Instant.now().minus(1, ChronoUnit.DAYS));

      when(refreshTokenRepository.findAllActiveByUserId(userId))
          .thenReturn(List.of(oldToken1, oldToken2, oldToken3, oldToken4, oldToken5));
      when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

      service.issueOnLogin(userId, null, null);

      verify(refreshTokenRepository, times(2)).save(any());
    }
  }

  @Nested
  @DisplayName("refresh()")
  class Refresh {

    @Test
    @DisplayName("rotates valid token and returns new tokens")
    void rotatesValidToken() {
      String rawToken = "test-raw-token";
      String tokenHash = RefreshToken.hashToken(rawToken);
      UUID userId = UUID.randomUUID();
      User user = User.createAdmin("Test", "test@example.com", "hash");
      RefreshToken oldToken = createToken(userId, Instant.now().plus(30, ChronoUnit.DAYS));

      when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(oldToken));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
      when(tokenIssuerPort.issue(user))
          .thenReturn(new LoginResponse("access", 900L, userId, "ADMIN", null, null, false));

      LoginResponse response = service.refresh(rawToken, "userAgent", "ip");

      assertNotNull(response);
      assertNotNull(response.refreshToken());
      assertNotEquals(rawToken, response.refreshToken());

      verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("throws on invalid token")
    void throwsOnInvalidToken() {
      String rawToken = "invalid-token";
      String tokenHash = RefreshToken.hashToken(rawToken);

      when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

      assertThrows(BadCredentialsException.class, () -> service.refresh(rawToken, null, null));
    }

    @Test
    @DisplayName("revokes all tokens on reuse detection")
    void revokesAllOnReuseDetection() {
      String rawToken = "reused-token";
      String tokenHash = RefreshToken.hashToken(rawToken);
      UUID userId = UUID.randomUUID();
      RefreshToken revokedToken = createToken(userId, Instant.now().plus(30, ChronoUnit.DAYS));
      revokedToken.revoke();

      when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revokedToken));

      assertThrows(BadCredentialsException.class, () -> service.refresh(rawToken, null, null));

      verify(refreshTokenRepository).revokeAllByUserId(userId);
    }
  }

  @Nested
  @DisplayName("logout()")
  class Logout {

    @Test
    @DisplayName("revokes all user tokens")
    void revokesAllTokens() {
      UUID userId = UUID.randomUUID();

      service.logout(userId);

      verify(refreshTokenRepository).revokeAllByUserId(userId);
    }
  }

  private RefreshToken createToken(UUID userId, Instant expiresAt) {
    return RefreshToken.issue(userId, "test-token-" + UUID.randomUUID(), expiresAt, null, null);
  }
}
