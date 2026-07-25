package com.evcharging.identity.infrastructure.security;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.application.port.out.TokenIssuerPort;
import com.evcharging.identity.domain.model.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Issues HS256-signed JWT access tokens.
 *
 * <p>Reads the signing secret from {@code app.jwt.secret} (Base64-encoded, ≥ 256-bit). Token expiry
 * is configured via {@code app.jwt.access-token-expiry-ms} (default: 15 min).
 */
@Service
class JwtIssuerService implements TokenIssuerPort {

  private final String secretBase64;
  private final long accessTokenExpiryMs;

  JwtIssuerService(
      @Value("${app.jwt.secret}") String secretBase64,
      @Value("${app.jwt.access-token-expiry-ms:900000}") long accessTokenExpiryMs) {
    this.secretBase64 = secretBase64;
    this.accessTokenExpiryMs = accessTokenExpiryMs;
  }

  /** {@inheritDoc} */
  @Override
  public LoginResponse issue(User user) {
    String roleName = user.getRole().name();
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", user.getEmail());
    claims.put("role", "ROLE_" + roleName);
    claims.put("roles", List.of("ROLE_" + roleName));
    claims.put("realm_access", Map.of("roles", List.of(roleName)));
    if (user.getVendorId() != null) {
      claims.put("vendor_id", user.getVendorId().toString());
    }
    if (user.getAccountNumber() != null) {
      claims.put("account_number", user.getAccountNumber());
    }

    long nowMs = System.currentTimeMillis();
    String token =
        Jwts.builder()
            .claims(claims)
            .subject(user.getId().toString())
            .issuedAt(new Date(nowMs))
            .expiration(new Date(nowMs + accessTokenExpiryMs))
            .signWith(signingKey(), Jwts.SIG.HS256)
            .compact();

    long expiresInSeconds = accessTokenExpiryMs / 1000;
    // Note: refreshToken and mustChangePassword are set by AuthenticationApplicationService
    return new LoginResponse(
        token, expiresInSeconds, user.getId(), roleName, user.getVendorId(), null, false);
  }

  SecretKey signingKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
  }
}
