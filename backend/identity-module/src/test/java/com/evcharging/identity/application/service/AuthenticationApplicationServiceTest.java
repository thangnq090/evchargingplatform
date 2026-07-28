package com.evcharging.identity.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.evcharging.identity.application.dto.LoginRequest;
import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.application.port.out.TokenIssuerPort;
import com.evcharging.identity.domain.model.Role;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;
import com.evcharging.identity.domain.repository.UserRepository;

@DisplayName("AuthenticationApplicationService")
@ExtendWith(MockitoExtension.class)
class AuthenticationApplicationServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private TokenIssuerPort tokenIssuerPort;
  @Mock private RefreshTokenApplicationService refreshTokenApplicationService;

  private AuthenticationApplicationService service;

  @BeforeEach
  void setUp() {
    service =
        new AuthenticationApplicationService(
            userRepository, passwordEncoder, tokenIssuerPort, refreshTokenApplicationService);
  }

  private User createUser(String email, String hash, UserStatus status) {
    return User.reconstitute(
        UUID.randomUUID(), "Test User", email, hash, null, Role.CUSTOMER,
        null, null, status, false, java.time.Instant.now(), java.time.Instant.now());
  }

  @Nested
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName("returns login response on success")
    void shouldLoginSuccessfully() {
      String email = "user@test.com";
      String hash = "$2a$12$hash";
      User user = createUser(email, hash, UserStatus.ACTIVE);
      LoginRequest request = new LoginRequest(email, "password123");

      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(passwordEncoder.matches("password123", hash)).willReturn(true);
      LoginResponse tokenResponse =
          new LoginResponse("jwt-token", 900, user.getId(), "CUSTOMER", null, null, false);
      given(tokenIssuerPort.issue(user)).willReturn(tokenResponse);
      given(refreshTokenApplicationService.issueOnLogin(user.getId(), null, null))
          .willReturn("refresh-token");

      LoginResponse result = service.login(request);

      assertThat(result.accessToken()).isEqualTo("jwt-token");
      assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("throws BadCredentialsException when email not found")
    void shouldThrowWhenEmailNotFound() {
      LoginRequest request = new LoginRequest("unknown@test.com", "password123");
      given(userRepository.findByEmail("unknown@test.com")).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.login(request))
          .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("throws BadCredentialsException when password wrong")
    void shouldThrowWhenPasswordWrong() {
      String email = "user@test.com";
      User user = createUser(email, "$2a$12$hash", UserStatus.ACTIVE);
      LoginRequest request = new LoginRequest(email, "wrongpassword");

      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(passwordEncoder.matches("wrongpassword", "$2a$12$hash")).willReturn(false);

      assertThatThrownBy(() -> service.login(request))
          .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("throws IllegalStateException when account not active")
    void shouldThrowWhenAccountNotActive() {
      String email = "user@test.com";
      User user = createUser(email, "$2a$12$hash", UserStatus.SUSPENDED);
      LoginRequest request = new LoginRequest(email, "password123");

      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(passwordEncoder.matches("password123", "$2a$12$hash")).willReturn(true);

      assertThatThrownBy(() -> service.login(request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("allows login with PASSWORD_RESET_REQUIRED status")
    void shouldAllowLoginWithPasswordResetRequired() {
      String email = "user@test.com";
      User user = createUser(email, "$2a$12$hash", UserStatus.PASSWORD_RESET_REQUIRED);
      user.initiatePasswordReset("$2a$12$temphash");
      LoginRequest request = new LoginRequest(email, "password123");

      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(passwordEncoder.matches("password123", "$2a$12$temphash")).willReturn(true);
      LoginResponse tokenResponse =
          new LoginResponse("jwt", 900, user.getId(), "CUSTOMER", null, null, true);
      given(tokenIssuerPort.issue(user)).willReturn(tokenResponse);
      given(refreshTokenApplicationService.issueOnLogin(user.getId(), null, null))
          .willReturn("refresh");

      LoginResponse result = service.login(request);

      assertThat(result.mustChangePassword()).isTrue();
    }

    @Test
    @DisplayName("login with userAgent and IP")
    void shouldLoginWithExtraParams() {
      String email = "user@test.com";
      User user = createUser(email, "$2a$12$hash", UserStatus.ACTIVE);
      LoginRequest request = new LoginRequest(email, "password123");

      given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
      given(passwordEncoder.matches("password123", "$2a$12$hash")).willReturn(true);
      LoginResponse tokenResponse =
          new LoginResponse("jwt", 900, user.getId(), "CUSTOMER", null, null, false);
      given(tokenIssuerPort.issue(user)).willReturn(tokenResponse);
      given(refreshTokenApplicationService.issueOnLogin(user.getId(), "Mozilla/5.0", "127.0.0.1"))
          .willReturn("refresh");

      LoginResponse result = service.login(request, "Mozilla/5.0", "127.0.0.1");

      assertThat(result.accessToken()).isEqualTo("jwt");
    }
  }
}
