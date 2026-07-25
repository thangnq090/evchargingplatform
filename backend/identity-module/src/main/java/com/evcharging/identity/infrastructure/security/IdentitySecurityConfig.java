package com.evcharging.identity.infrastructure.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.evcharging.shared.security.PlatformJwtAuthenticationConverter;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Security configuration for the identity module (WebFlux Reactive).
 *
 * <p>Stateless JWT resource server using HS256 (HMAC-SHA256). The same secret used to issue tokens
 * in {@link JwtIssuerService} is used here to verify them — no JWKS endpoint required for the MVP.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class IdentitySecurityConfig {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  /** BCrypt strength 12 — matches ADR-007 and the spring-security-jwt skill requirements. */
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  /**
   * HS256 in-process JWT decoder — uses the same HMAC secret as {@link JwtIssuerService}. No remote
   * JWKS fetch needed.
   */
  @Bean
  ReactiveJwtDecoder reactiveJwtDecoder() {
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    return NimbusReactiveJwtDecoder.withSecretKey(key)
        .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
        .build();
  }

  @Bean
  SecurityWebFilterChain identitySecurityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .authorizeExchange(
            auth ->
                auth.pathMatchers(
                        "/api/v1/identity/auth/login",
                        "/api/v1/identity/auth/register-customer",
                        "/api/v1/identity/auth/invitations/accept",
                        "/api/v1/identity/auth/refresh")
                    .permitAll()
                    .pathMatchers("/api/v1/identity/vendors/**")
                    .hasRole("ADMIN")
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtAuthenticationConverter(
                            new ReactiveJwtAuthenticationConverterAdapter(
                                new PlatformJwtAuthenticationConverter()))))
        .build();
  }
}
