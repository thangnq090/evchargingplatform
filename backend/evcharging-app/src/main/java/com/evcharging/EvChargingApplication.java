package com.evcharging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the EV Charging Platform.
 *
 * <p>This is the <strong>composition root</strong> of the modular monolith. It assembles all
 * business modules and configures shared infrastructure.
 *
 * <p><strong>Responsibilities:</strong>
 *
 * <ul>
 *   <li>Bootstraps the Spring Boot application
 *   <li>Imports all business module configurations
 *   <li>Configures shared infrastructure (Jackson, Flyway, OpenAPI, Security)
 * </ul>
 *
 * <p><strong>Does NOT contain:</strong> Business logic, domain models, or module-specific
 * configuration. Those belong in their respective modules.
 *
 * <p>Module boundaries are detected automatically by Spring Modulith based on the top-level package
 * structure under {@code com.evcharging} (e.g. {@code identity}, {@code station}, {@code session}).
 */
@SpringBootApplication
@EnableAsync
public class EvChargingApplication {

  public static void main(String[] args) {
    SpringApplication.run(EvChargingApplication.class, args);
  }
}
