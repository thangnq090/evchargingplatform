package com.evcharging.identity.application.service;

import com.evcharging.identity.application.dto.LoginRequest;
import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.application.port.out.TokenIssuerPort;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;
import com.evcharging.identity.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

  public AuthenticationApplicationService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      TokenIssuerPort tokenIssuerPort) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenIssuerPort = tokenIssuerPort;
  }

  /**
   * Authenticate a user and issue a JWT access token.
   *
   * @param request login credentials
   * @return JWT token response
   * @throws ResponseStatusException 401 if credentials are invalid, 403 if account is inactive
   */
  public LoginResponse login(LoginRequest request) {
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

    if (user.getStatus() != UserStatus.ACTIVE) {
      log.warn("Login failed — account not active: {} status={}", email, user.getStatus());
      throw new IllegalStateException("Account is not active");
    }

    log.info("Login successful: userId={}, role={}", user.getId(), user.getRole());
    return tokenIssuerPort.issue(user);
  }
}
