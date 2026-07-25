package com.evcharging.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Standalone Spring Boot application for Spring Cloud Gateway.
 *
 * <p>Operates on the reactive WebFlux/Netty stack. Handles API routing, JWT validation, rate
 * limiting, and CORS.
 */
@SpringBootApplication
public class GatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
  }
}
