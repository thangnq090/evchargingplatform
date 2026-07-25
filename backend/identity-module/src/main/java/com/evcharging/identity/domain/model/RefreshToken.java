package com.evcharging.identity.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain aggregate root representing a User's stateful Refresh Token.
 *
 * <p>Implements refresh token rotation and reuse detection. Stored in DB as a SHA-256 hash.
 */
public class RefreshToken {

  private final UUID id;
  private final UUID userId;
  private final String tokenHash;
  private final Instant issuedAt;
  private final Instant expiresAt;
  private Instant revokedAt;
  private UUID replacedByTokenId;
  private final String userAgent;
  private final String ipAddress;

  private RefreshToken(
      UUID id,
      UUID userId,
      String tokenHash,
      Instant issuedAt,
      Instant expiresAt,
      Instant revokedAt,
      UUID replacedByTokenId,
      String userAgent,
      String ipAddress) {
    this.id = id;
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
    this.revokedAt = revokedAt;
    this.replacedByTokenId = replacedByTokenId;
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
  }

  /**
   * Factory method to issue a new RefreshToken.
   *
   * @param userId the owner user
   * @param rawToken the unhashed, random token string
   * @param expiresAt expiry timestamp
   * @param userAgent calling user agent (optional)
   * @param ipAddress calling IP address (optional)
   */
  public static RefreshToken issue(
      UUID userId, String rawToken, Instant expiresAt, String userAgent, String ipAddress) {
    Objects.requireNonNull(userId, "userId is required");
    Objects.requireNonNull(rawToken, "rawToken is required");
    Objects.requireNonNull(expiresAt, "expiresAt is required");
    return new RefreshToken(
        UUID.randomUUID(),
        userId,
        hashToken(rawToken),
        Instant.now(),
        expiresAt,
        null,
        null,
        userAgent,
        ipAddress);
  }

  /** Reconstitute a RefreshToken from persistence. */
  public static RefreshToken reconstitute(
      UUID id,
      UUID userId,
      String tokenHash,
      Instant issuedAt,
      Instant expiresAt,
      Instant revokedAt,
      UUID replacedByTokenId,
      String userAgent,
      String ipAddress) {
    return new RefreshToken(
        id,
        userId,
        tokenHash,
        issuedAt,
        expiresAt,
        revokedAt,
        replacedByTokenId,
        userAgent,
        ipAddress);
  }

  /**
   * Rotate this token by revoking it and mapping it to its replacement.
   *
   * @param newTokenId the ID of the new replacement refresh token
   */
  public void rotate(UUID newTokenId) {
    if (this.revokedAt != null) {
      throw new IllegalStateException("Token already revoked");
    }
    this.revokedAt = Instant.now();
    this.replacedByTokenId = newTokenId;
  }

  /** Revoke this token. */
  public void revoke() {
    if (this.revokedAt == null) {
      this.revokedAt = Instant.now();
    }
  }

  /** Check if the token is valid at the given timestamp. */
  public boolean isValid(Instant now) {
    return this.revokedAt == null && this.expiresAt.isAfter(now);
  }

  /** Utility helper to hash a raw token using SHA-256. */
  public static String hashToken(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public UUID getReplacedByTokenId() {
    return replacedByTokenId;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getIpAddress() {
    return ipAddress;
  }
}
