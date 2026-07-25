package com.evcharging.config;

import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway configuration for multi-module database migrations.
 *
 * <p>Each module has its own migration directory under {@code db/migration/{module-name}/}. This
 * configuration ensures all module migrations are discovered and executed.
 */
@Configuration
public class FlywayConfig {

  /**
   * List of all module migration locations. Each module maintains its own migration scripts for
   * schema isolation.
   */
  private static final List<String> MODULE_MIGRATION_LOCATIONS =
      List.of(
          "classpath:db/migration/evcharging-app",
          "classpath:db/migration/gateway-module",
          "classpath:db/migration/identity-module",
          "classpath:db/migration/station-module",
          "classpath:db/migration/session-module",
          "classpath:db/migration/billing-module",
          "classpath:db/migration/payment-module",
          "classpath:db/migration/vehicle-module",
          "classpath:db/migration/notification-module",
          "classpath:db/migration/device-gateway-module");

  @Bean
  public FlywayConfigurationCustomizer flywayConfigurationCustomizer(DataSource dataSource) {
    return flyway ->
        flyway
            .locations(MODULE_MIGRATION_LOCATIONS.toArray(String[]::new))
            .baselineOnMigrate(true)
            .validateOnMigrate(true)
            .outOfOrder(false)
            .table("evcharging_platform_flyway_schema_history");
  }

  /**
   * Additional Flyway bean for programmatic access if needed. The main configuration is handled by
   * Spring Boot auto-configuration customized by {@link
   * #flywayConfigurationCustomizer(DataSource)}.
   */
  @Bean
  public Flyway flyway(DataSource dataSource) {
    Flyway flyway =
        Flyway.configure()
            .dataSource(dataSource)
            .locations(MODULE_MIGRATION_LOCATIONS.toArray(String[]::new))
            .baselineOnMigrate(true)
            .validateOnMigrate(true)
            .outOfOrder(false)
            .table("evcharging_platform_flyway_schema_history")
            .load();
    return flyway;
  }
}
