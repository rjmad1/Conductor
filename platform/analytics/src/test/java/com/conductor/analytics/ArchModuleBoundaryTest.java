package com.conductor.analytics;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * ArchUnit test verifying the analytics module respects modular monolith boundaries. The analytics
 * domain must NOT import from other platform domain packages.
 */
class ArchModuleBoundaryTest {

  private static JavaClasses classes;

  @BeforeAll
  static void setup() {
    classes =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.conductor.analytics");
  }

  @Test
  void analyticsDoesNotImportWorkflow() {
    noClasses()
        .that()
        .resideInAPackage("com.conductor.analytics..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.conductor.workflow..")
        .check(classes);
  }

  @Test
  void analyticsDoesNotImportCustomer() {
    noClasses()
        .that()
        .resideInAPackage("com.conductor.analytics..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.conductor.customer..")
        .check(classes);
  }

  @Test
  void analyticsDoesNotImportMessaging() {
    noClasses()
        .that()
        .resideInAPackage("com.conductor.analytics..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.conductor.messaging..")
        .check(classes);
  }

  @Test
  void analyticsDoesNotImportIntegrations() {
    noClasses()
        .that()
        .resideInAPackage("com.conductor.analytics..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.conductor.integrations..")
        .check(classes);
  }

  @Test
  void analyticsDoesNotImportIdentity() {
    noClasses()
        .that()
        .resideInAPackage("com.conductor.analytics..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.conductor.identity..")
        .check(classes);
  }

  @Test
  void analyticsDoesNotImportTenant() {
    noClasses()
        .that()
        .resideInAPackage("com.conductor.analytics..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.conductor.tenant..")
        .check(classes);
  }
}
