package com.evcharging.identity.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.evcharging.identity.application.dto.LoginRequest;
import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.application.port.out.TokenIssuerPort;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;
import com.evcharging.identity.domain.repository.UserRepository;

/**
 * Application service handling user authentication (login).
 *
 * <p>Validates credentials, checks account status, and issues a JWT access token.
 */
@Service
public class AuthenticationApplicationService {

  private static final Logger log = LoggerFactory.getLogger(AuthenticationApplicationService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenIssuerPort tokenIssuerPort;
  private final RefreshTokenApplicationService refreshTokenApplicationService;

  public AuthenticationApplicationService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      TokenIssuerPort tokenIssuerPort,
      RefreshTokenApplicationService refreshTokenApplicationService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenIssuerPort = tokenIssuerPort;
    this.refreshTokenApplicationService = refreshTokenApplicationService;
  }

  /**
   * Authenticate a user and issue a JWT access token.
   *
   * @param request login credentials
   * @return JWT token response
   * @throws ResponseStatusException 401 if credentials are invalid, 403 if account is inactive
   */
  public LoginResponse login(LoginRequest request) {
    return login(request, null, null);
  }

  /** Authenticate a user and issue JWT tokens with userAgent/IP headers. */
  @org.springframework.transaction.annotation.Transactional
  public LoginResponse login(LoginRequest request, String userAgent, String ip) {
    String email = request.email().toLowerCase();

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> {
                  log.warn("Login failed — email not found: {}", email);
                  return new BadCredentialsException("Invalid email or password");
                });

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      log.warn("Login failed — wrong password for: {}", email);
      throw new BadCredentialsException("Invalid email or password");
    }

    if (user.getStatus() != UserStatus.ACTIVE
        && user.getStatus() != UserStatus.PASSWORD_RESET_REQUIRED) {
      log.warn("Login failed — account not active: {} status={}", email, user.getStatus());
      throw new IllegalStateException("Account is not active");
    }

    log.info("Login successful: userId={}, role={}", user.getId(), user.getRole());
    LoginResponse baseResponse = tokenIssuerPort.issue(user);

    // Issue refresh token
    String refreshToken = refreshTokenApplicationService.issueOnLogin(user.getId(), userAgent, ip);

    return new LoginResponse(
        baseResponse.accessToken(),
        baseResponse.expiresIn(),
        baseResponse.userId(),
        baseResponse.role(),
        baseResponse.vendorId(),
        refreshToken,
        user.isMustChangePassword());
  }
}
