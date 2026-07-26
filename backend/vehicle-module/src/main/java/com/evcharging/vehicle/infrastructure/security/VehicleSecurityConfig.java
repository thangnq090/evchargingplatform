package com.evcharging.vehicle.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import com.evcharging.shared.security.PlatformJwtAuthenticationConverter;

/** Security configuration for the Vehicle module. */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class VehicleSecurityConfig {

  @Bean
  @Order(94)
  public SecurityWebFilterChain vehicleSecurityWebFilterChain(ServerHttpSecurity http) {
    return http.securityMatcher(
            ServerWebExchangeMatchers.pathMatchers(
                "/api/v1/vehicles/**", "/api/v1/admin/vehicles/**"))
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .authorizeExchange(auth -> auth.anyExchange().authenticated())
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
