package com.evcharging.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

/**
 * Security configuration for Spring Cloud Gateway.
 *
 * <p>Configures JWT authentication for all routes except actuator and public endpoints. Uses
 * reactive security for WebFlux-based gateway.
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

  /**
   * Security filter chain for the gateway.
   *
   * <p>All routes require JWT authentication except:
   *
   * <ul>
   *   <li>Actuator health/info endpoints
   *   <li>OpenAPI/Swagger endpoints
   *   <li>OCPP WebSocket endpoints (handled separately)
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
                    .pathMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .pathMatchers("/ocpp/**")
                    .permitAll() // OCPP handled by device-gateway module
                    // All other endpoints require authentication
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(
                        jwt ->
                            jwt.jwtAuthenticationConverter(
                                new ReactiveJwtAuthenticationConverterAdapter(
                                    new GatewayJwtAuthenticationConverter())))
                    .authenticationEntryPoint(
                        new HttpStatusServerEntryPoint(
                            org.springframework.http.HttpStatus.UNAUTHORIZED)))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    new HttpStatusServerEntryPoint(
                        org.springframework.http.HttpStatus.UNAUTHORIZED)))
        .build();
  }
}
