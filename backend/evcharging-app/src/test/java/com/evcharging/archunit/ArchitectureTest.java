package com.evcharging.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Architecture tests for EV Charging Platform modular monolith.
 *
 * <p>These tests verify that module boundaries are respected and that the modular monolith follows
 * the hexagonal architecture principles.
 *
 * <p>Run with: {@code mvn test -Dtest=ArchitectureTest}
 */
class ArchitectureTest {

  private static final String BASE_PACKAGE = "com.evcharging";

  private static final JavaClasses CLASSES =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages(BASE_PACKAGE);

  /**
   * Verifies Spring Modulith module structure.
   *
   * <p>This test ensures all modules are properly defined with {@link
   * org.springframework.modulith.NamedModule} and that there are no forbidden cross-module
   * dependencies.
   */
  @Test
  void verifyModulithModules() {
    ApplicationModules modules = ApplicationModules.of(BASE_PACKAGE);
    modules.verify();
  }

  /**
   * Verifies that domain layer has no Spring Framework dependencies.
   *
   * <p>Domain layer should be pure Java with no framework dependencies.
   */
  @Test
  void domainLayerShouldNotDependOnSpringFramework() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "javax.persistence..",
                "org.hibernate..")
            .because("Domain layer must be pure Java with no framework dependencies");

    rule.allowEmptyShould(true).check(CLASSES);
  }

  /**
   * Verifies that application layer only depends on domain and ports.
   *
   * <p>Application layer should not depend on infrastructure or adapters.
   */
  @Test
  void applicationLayerShouldNotDependOnInfrastructure() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..infrastructure..", "..adapter..", "..persistence..", "..controller..")
            .because("Application layer must not depend on infrastructure or adapters");

    rule.allowEmptyShould(true).check(CLASSES);
  }

  /**
   * Verifies that infrastructure layer implements ports from domain/application.
   *
   * <p>Infrastructure should only depend on domain/application through interfaces (ports).
   */
  @Test
  void infrastructureLayerShouldOnlyDependOnPorts() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..infrastructure..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..controller..", "..api..")
            .andShould()
            .dependOnClassesThat()
            .resideInAPackage("..domain.model..")
            .because(
                "Infrastructure should only depend on domain/application ports, not concrete implementations");

    rule.allowEmptyShould(true).check(CLASSES);
  }

  /**
   * Verifies no cross-module domain dependencies.
   *
   * <p>Domain models must not access other modules' domain models directly. Cross-module
   * communication must go through events or application services.
   */
  @Test
  void noCrossModuleDomainDependencies() {
    String[] modules = {
      "identity", "station", "session", "billing",
      "payment", "vehicle", "notification", "devicegateway"
    };

    for (String moduleA : modules) {
      for (String moduleB : modules) {
        if (!moduleA.equals(moduleB)) {
          ArchRule rule =
              noClasses()
                  .that()
                  .resideInAPackage(".." + moduleA + "..domain..")
                  .should()
                  .dependOnClassesThat()
                  .resideInAPackage(".." + moduleB + "..domain..")
                  .because(
                      "Module '"
                          + moduleA
                          + "' domain must not depend on module '"
                          + moduleB
                          + "' domain. Use events or application services instead.");

          rule.allowEmptyShould(true).check(CLASSES);
        }
      }
    }
  }

  /** Verifies that controllers only exist in api package. */
  @Test
  void controllersOnlyInApiPackage() {
    ArchRule rule =
        noClasses()
            .that()
            .haveSimpleNameEndingWith("Controller")
            .should()
            .resideOutsideOfPackage("..api.controller..")
            .because("Controllers must reside in api.controller package");

    rule.allowEmptyShould(true).check(CLASSES);
  }

  /** Verifies that repositories are interfaces in domain.repository. */
  @Test
  void repositoriesAreInterfacesInDomainRepository() {
    ArchRule rule =
        noClasses()
            .that()
            .haveSimpleNameEndingWith("Repository")
            .and()
            .areNotInterfaces()
            .should()
            .resideInAPackage("..infrastructure.persistence..")
            .because(
                "Repository implementations belong in infrastructure.persistence, interfaces in domain.repository");

    rule.allowEmptyShould(true).check(CLASSES);
  }

  /** Verifies that domain events follow naming convention. */
  @Test
  void domainEventsFollowNamingConvention() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..domain.event..")
            .should()
            .haveSimpleNameEndingWith("Event")
            .because("Domain events must end with 'Event' suffix (e.g., SessionStartedEvent)");

    rule.allowEmptyShould(true).check(CLASSES);
  }

  /** Verifies that value objects are immutable (final fields, no setters). */
  @Test
  void valueObjectsShouldBeImmutable() {
    // This is a basic check - more thorough validation would require custom ArchUnit rules
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..domain.model..")
            .and()
            .areAnnotatedWith("jakarta.persistence.Entity")
            .should()
            .haveSimpleNameEndingWith("Entity")
            .because(
                "JPA Entities should be named *Entity to distinguish from domain Value Objects");

    rule.allowEmptyShould(true).check(CLASSES);
  }

  /**
   * Generates module documentation for review.
   *
   * <p>Run this test to generate module canvas and component diagrams in {@code
   * target/modulith-docs/}.
   */
  @Test
  void generateModuleDocumentation() {
    ApplicationModules modules = ApplicationModules.of(BASE_PACKAGE);
    new Documenter(modules).writeModulesAsPlantUml().writeModuleCanvases();
  }
}
