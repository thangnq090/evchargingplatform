package com.evcharging.session.infrastructure.security;

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

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SessionSecurityConfig {

  @Bean
  @Order(99) // High priority to run before the identity global filter chain
  public SecurityWebFilterChain sessionSecurityWebFilterChain(ServerHttpSecurity http) {
    return http.securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/v1/sessions/**"))
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .authorizeExchange(
            auth ->
                auth.pathMatchers("/api/v1/sessions/*/meter-readings")
                    .permitAll()
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
