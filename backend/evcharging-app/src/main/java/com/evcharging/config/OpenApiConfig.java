package com.evcharging.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * SpringDoc OpenAPI configuration for EV Charging Platform API documentation.
 *
 * <p>Configures:
 *
 * <ul>
 *   <li>API info (title, version, contact, license)
 *   <li>JWT Bearer authentication scheme
 *   <li>Server URLs for different environments
 * </ul>
 */
@Configuration
public class OpenApiConfig {

  @Value("${spring.application.name:evcharging-platform}")
  private String applicationName;

  @Value("${server.port:8080}")
  private String serverPort;

  @Value("${server.servlet.context-path:/api}")
  private String contextPath;

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(apiInfo())
        .servers(
            List.of(
                new Server()
                    .url("http://localhost:" + serverPort + contextPath)
                    .description("Development Server"),
                new Server()
                    .url("https://api.evcharging.example.com" + contextPath)
                    .description("Production Server")))
        .components(new Components().addSecuritySchemes("bearerAuth", securityScheme()))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }

  private Info apiInfo() {
    return new Info()
        .title("EV Charging Platform API")
        .version("1.0.0")
        .description(
            """
                Modular Monolith API for EV Charging Management Platform.

                ## Modules
                - **Identity & Access**: Authentication, Authorization, User Management, RBAC
                - **Station Management**: Station Registry, Connectors, Health Monitoring, Firmware
                - **Session Management**: Charging Session Lifecycle, Metering, State Machine
                - **Billing & Pricing**: Tariffs, Cost Calculation, Invoicing
                - **Payment Processing**: Payment Orchestration, Provider Integration
                - **Vehicle Management**: Vehicle Registry, RFID, Ownership Transfer
                - **Notifications**: Multi-channel Delivery, Templates, Preferences
                - **Device Gateway**: OCPP Protocol Handling, Message Routing, Device Auth

                ## Authentication
                All endpoints require JWT Bearer token authentication via Spring Cloud Gateway.
                Tokens are validated using RS256 with JWKS endpoint.

                ## Error Responses
                All errors follow RFC 7807 (Problem Details for HTTP APIs) format.
                """)
        .contact(
            new Contact()
                .name("EV Charging Platform Team")
                .email("platform@evcharging.example.com")
                .url("https://evcharging.example.com"))
        .license(new License().name("Proprietary").url("https://evcharging.example.com/license"));
  }

  private SecurityScheme securityScheme() {
    return new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description(
            "JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"");
  }
}
