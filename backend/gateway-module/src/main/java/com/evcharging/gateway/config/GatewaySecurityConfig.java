package com.evcharging.gateway.config;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

import com.evcharging.shared.security.PlatformJwtAuthenticationConverter;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Security configuration for Spring Cloud Gateway.
 *
 * <p>Configures HS256 JWT authentication for all routes except actuator and public endpoints. Uses
 * the same HMAC secret as the identity module — no JWKS endpoint required for the MVP.
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  /**
   * HS256 in-process JWT decoder — uses the same HMAC secret as the identity module's
   * JwtIssuerService. No remote JWKS fetch needed.
   */
  @Bean
  public ReactiveJwtDecoder reactiveJwtDecoder() {
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    return NimbusReactiveJwtDecoder.withSecretKey(key)
        .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
        .build();
  }

  /**
   * Security filter chain for the gateway.
   *
   * <p>All routes require JWT authentication except:
   *
   * <ul>
   *   <li>Actuator health/info endpoints
   *   <li>OpenAPI/Swagger endpoints
   *   <li>OCPP WebSocket endpoints (handled separately)
   *   <li>Identity auth endpoints (register, login, invitation accept)
   * </ul>
   */
  @Bean
  public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
    return http.csrf(csrf -> csrf.disable())
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .logout(logout -> logout.disable())
        .authorizeExchange(
            auth ->
                auth
                    // Public endpoints
                    .pathMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus")
                    .permitAll()
                    .pathMatchers(
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**")
                    .permitAll()
                    .pathMatchers("/ocpp/**")
                    .permitAll() // OCPP handled by device-gateway module
                    .pathMatchers("/api/v1/identity/auth/**")
                    .permitAll() // Public registration, login, and invitation endpoints
                    // All other endpoints require authentication
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtAuthenticationConverter(
                            new ReactiveJwtAuthenticationConverterAdapter(
                                new PlatformJwtAuthenticationConverter()))))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    new HttpStatusServerEntryPoint(
                        org.springframework.http.HttpStatus.UNAUTHORIZED)))
        .build();
  }
}
