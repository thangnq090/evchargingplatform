package com.evcharging.station.infrastructure.security;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import reactor.test.StepVerifier;

@DisplayName("VendorSecurity")
class VendorSecurityTest {

  private final VendorSecurity vendorSecurity = new VendorSecurity();

  @Nested
  @DisplayName("checkAccess")
  class CheckAccess {

    @Test
    @DisplayName("returns true for platform admin")
    void shouldReturnTrueForAdmin() {
      UUID vendorId = UUID.randomUUID();
      List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
      TestingAuthenticationToken auth = new TestingAuthenticationToken("user", "pass", authorities);

      StepVerifier.create(
              vendorSecurity
                  .checkAccess(vendorId)
                  .contextWrite(
                      ReactiveSecurityContextHolder.withAuthentication(auth)))
          .expectNext(true)
          .verifyComplete();
    }

    @Test
    @DisplayName("returns true for vendor admin owning the vendor")
    void shouldReturnTrueForVendorOwner() {
      UUID vendorId = UUID.randomUUID();
      Jwt jwt =
          Jwt.withTokenValue("token")
              .header("alg", "RS256")
              .subject("user-1")
              .claim("vendor_id", vendorId.toString())
              .issuedAt(Instant.now())
              .expiresAt(Instant.now().plusSeconds(3600))
              .build();

      List<SimpleGrantedAuthority> authorities =
          List.of(new SimpleGrantedAuthority("ROLE_VENDOR_ADMIN"));
      JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);

      StepVerifier.create(
              vendorSecurity
                  .checkAccess(vendorId)
                  .contextWrite(
                      ReactiveSecurityContextHolder.withAuthentication(auth)))
          .expectNext(true)
          .verifyComplete();
    }

    @Test
    @DisplayName("returns false for vendor admin not owning the vendor")
    void shouldReturnFalseForVendorNotOwner() {
      UUID vendorId = UUID.randomUUID();
      UUID otherVendorId = UUID.randomUUID();
      Jwt jwt =
          Jwt.withTokenValue("token")
              .header("alg", "RS256")
              .subject("user-1")
              .claim("vendor_id", otherVendorId.toString())
              .issuedAt(Instant.now())
              .expiresAt(Instant.now().plusSeconds(3600))
              .build();

      List<SimpleGrantedAuthority> authorities =
          List.of(new SimpleGrantedAuthority("ROLE_VENDOR_ADMIN"));
      JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);

      StepVerifier.create(
              vendorSecurity
                  .checkAccess(vendorId)
                  .contextWrite(
                      ReactiveSecurityContextHolder.withAuthentication(auth)))
          .expectNext(false)
          .verifyComplete();
    }

    @Test
    @DisplayName("returns false for regular user")
    void shouldReturnFalseForRegularUser() {
      UUID vendorId = UUID.randomUUID();
      List<SimpleGrantedAuthority> authorities =
          List.of(new SimpleGrantedAuthority("ROLE_USER"));
      TestingAuthenticationToken auth = new TestingAuthenticationToken("user", "pass", authorities);

      StepVerifier.create(
              vendorSecurity
                  .checkAccess(vendorId)
                  .contextWrite(
                      ReactiveSecurityContextHolder.withAuthentication(auth)))
          .expectNext(false)
          .verifyComplete();
    }

    @Test
    @DisplayName("returns false when no security context")
    void shouldReturnFalseWhenNoContext() {
      StepVerifier.create(vendorSecurity.checkAccess(UUID.randomUUID()))
          .expectNext(false)
          .verifyComplete();
    }
  }
}
