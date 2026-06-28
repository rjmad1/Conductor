package com.conductor.referenceapp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ArchitectureTest {

  private static JavaClasses importedClasses;

  @BeforeAll
  public static void setUp() {
    importedClasses =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.conductor");
  }

  @Test
  @DisplayName("Platform modules should not depend on each other directly")
  void platformModulesShouldNotDependOnEachOther() {
    // Example: platform.customer should not depend on platform.workflow directly.
    // They must communicate through shared interfaces or NATS events.
    noClasses()
        .that()
        .resideInAPackage("com.conductor.customer..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "com.conductor.workflow..",
            "com.conductor.tenant..",
            "com.conductor.integrations..",
            "com.conductor.analytics..")
        .check(importedClasses);

    noClasses()
        .that()
        .resideInAPackage("com.conductor.workflow..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "com.conductor.customer..",
            "com.conductor.tenant..",
            "com.conductor.integrations..",
            "com.conductor.analytics..")
        .check(importedClasses);

    noClasses()
        .that()
        .resideInAPackage("com.conductor.analytics..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "com.conductor.customer..",
            "com.conductor.tenant..",
            "com.conductor.integrations..",
            "com.conductor.workflow..")
        .check(importedClasses);
  }

  @Test
  @DisplayName("Shared modules should not depend on platform modules")
  void sharedModulesShouldNotDependOnPlatformModules() {
    noClasses()
        .that()
        .resideInAPackage("com.conductor.shared..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "com.conductor.customer..",
            "com.conductor.workflow..",
            "com.conductor.tenant..",
            "com.conductor.integrations..",
            "com.conductor.analytics..",
            "com.conductor.ai..")
        .check(importedClasses);
  }
}
