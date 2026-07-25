package com.evcharging.identity.application.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.application.port.out.TokenIssuerPort;
import com.evcharging.identity.domain.model.RefreshToken;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;
import com.evcharging.identity.domain.repository.RefreshTokenRepository;
import com.evcharging.identity.domain.repository.UserRepository;

@Service
public class RefreshTokenApplicationService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final TokenIssuerPort tokenIssuerPort;
  private final long refreshExpiryDays;

  public RefreshTokenApplicationService(
      RefreshTokenRepository refreshTokenRepository,
      UserRepository userRepository,
      TokenIssuerPort tokenIssuerPort,
      @Value("${app.jwt.refresh-token-expiry-days:30}") long refreshExpiryDays) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
    this.tokenIssuerPort = tokenIssuerPort;
    this.refreshExpiryDays = refreshExpiryDays;
  }

  @Transactional
  public String issueOnLogin(UUID userId, String userAgent, String ip) {
    // Enforce max 5 active tokens limit
    List<RefreshToken> active = refreshTokenRepository.findAllActiveByUserId(userId);
    if (active.size() >= 5) {
      active.stream()
          .min(Comparator.comparing(RefreshToken::getIssuedAt))
          .ifPresent(
              oldest -> {
                oldest.revoke();
                refreshTokenRepository.save(oldest);
              });
    }

    String rawToken = generateSecureToken();
    Instant expiresAt = Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS);
    RefreshToken token = RefreshToken.issue(userId, rawToken, expiresAt, userAgent, ip);
    refreshTokenRepository.save(token);
    return rawToken;
  }

  @Transactional
  public LoginResponse refresh(String rawToken, String userAgent, String ip) {
    String hash = RefreshToken.hashToken(rawToken);
    RefreshToken token =
        refreshTokenRepository
            .findByTokenHash(hash)
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    // Reuse detection
    if (token.getRevokedAt() != null) {
      refreshTokenRepository.revokeAllByUserId(token.getUserId());
      throw new BadCredentialsException("Token reuse detected. All sessions revoked.");
    }

    if (!token.isValid(Instant.now())) {
      throw new BadCredentialsException("Expired refresh token");
    }

    User user =
        userRepository
            .findById(token.getUserId())
            .orElseThrow(() -> new BadCredentialsException("User not found"));

    if (user.getStatus() == UserStatus.SUSPENDED) {
      throw new IllegalStateException("User is suspended");
    }

    // Rotate token
    String newRawToken = generateSecureToken();
    Instant expiresAt = Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS);
    RefreshToken newToken = RefreshToken.issue(user.getId(), newRawToken, expiresAt, userAgent, ip);
    RefreshToken savedNew = refreshTokenRepository.save(newToken);

    token.rotate(savedNew.getId());
    refreshTokenRepository.save(token);

    LoginResponse baseResponse = tokenIssuerPort.issue(user);
    return new LoginResponse(
        baseResponse.accessToken(),
        baseResponse.expiresIn(),
        baseResponse.userId(),
        baseResponse.role(),
        baseResponse.vendorId(),
        newRawToken,
        user.isMustChangePassword());
  }

  @Transactional
  public void logout(UUID userId) {
    refreshTokenRepository.revokeAllByUserId(userId);
  }

  private String generateSecureToken() {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
