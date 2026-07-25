package com.evcharging.gateway.config;

import java.util.List;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Spring Cloud Gateway configuration.
 *
 * <p>Defines routes to backend modules and global CORS configuration.
 */
@Configuration
public class GatewayConfig {

  /**
   * Configures API routes for all backend modules.
   *
   * <p>Routes are prefixed with /api/v1/{module} and forwarded to the corresponding module
   * controllers.
   */
  @Bean
  public RouteLocator routes(RouteLocatorBuilder builder) {
    return builder
        .routes()
        // Identity & Access routes
        .route(
            "identity",
            r ->
                r.path("/api/v1/identity/**")
                    .filters(f -> f.stripPrefix(2))
                    .uri("lb://identity-module"))

        // Station Management routes
        .route(
            "station",
            r ->
                r.path("/api/v1/stations/**")
                    .filters(f -> f.stripPrefix(2))
                    .uri("lb://station-module"))

        // Session Management routes
        .route(
            "session",
            r ->
                r.path("/api/v1/sessions/**")
                    .filters(f -> f.stripPrefix(2))
                    .uri("lb://session-module"))

        // Billing & Pricing routes
        .route(
            "billing",
            r ->
                r.path("/api/v1/billing/**")
                    .filters(f -> f.stripPrefix(2))
                    .uri("lb://billing-module"))

        // Payment Processing routes
        .route(
            "payment",
            r ->
                r.path("/api/v1/payments/**")
                    .filters(f -> f.stripPrefix(2))
                    .uri("lb://payment-module"))

        // Vehicle Management routes
        .route(
            "vehicle",
            r ->
                r.path("/api/v1/vehicles/**")
                    .filters(f -> f.stripPrefix(2))
                    .uri("lb://vehicle-module"))

        // Notification routes
        .route(
            "notification",
            r ->
                r.path("/api/v1/notifications/**")
                    .filters(f -> f.stripPrefix(2))
                    .uri("lb://notification-module"))

        // Device Gateway routes (OCPP)
        .route(
            "device-gateway",
            r ->
                r.path("/api/v1/device-gateway/**", "/ocpp/**")
                    .filters(f -> f.stripPrefix(2))
                    .uri("lb://device-gateway-module"))

        // Actuator endpoints (health, metrics, etc.) - no prefix strip
        .route("actuator", r -> r.path("/actuator/**").uri("lb://evcharging-app"))

        // Swagger/OpenAPI
        .route(
            "api-docs",
            r ->
                r.path("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .uri("lb://evcharging-app"))
        .build();
  }

  /**
   * Global CORS configuration for the gateway.
   *
   * <p>Allows requests from configured origins with credentials.
   */
  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(
        List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://app.evcharging.example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("X-Correlation-ID", "X-Request-ID"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsWebFilter(source);
  }
}
