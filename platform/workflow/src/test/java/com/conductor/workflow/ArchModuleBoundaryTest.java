package com.conductor.workflow;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ArchUnit module boundary tests. Enforces that the workflow domain does not import from other
 * bounded contexts per ARCHITECTURE_GUARDRAILS.md G-001 (No Cross-Boundary Imports).
 */
class ArchModuleBoundaryTest {

  private static JavaClasses workflowClasses;

  @BeforeAll
  static void loadClasses() {
    workflowClasses = new ClassFileImporter().importPackages("com.conductor.workflow");
  }

  @Test
  @DisplayName("Workflow must not import from identity domain")
  void noImportFromIdentity() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("com.conductor.workflow..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.conductor.identity..");
    rule.check(workflowClasses);
  }

  @Test
  @DisplayName("Workflow must not import from customer domain")
  void noImportFromCustomer() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("com.conductor.workflow..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.conductor.customer..");
    rule.check(workflowClasses);
  }

  @Test
  @DisplayName("Workflow must not import from integrations domain")
  void noImportFromIntegrations() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("com.conductor.workflow..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.conductor.integrations..");
    rule.check(workflowClasses);
  }

  @Test
  @DisplayName("Workflow must not import from messaging domain")
  void noImportFromMessaging() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("com.conductor.workflow..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.conductor.messaging..");
    rule.check(workflowClasses);
  }

  @Test
  @DisplayName("Workflow must not import from analytics domain")
  void noImportFromAnalytics() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("com.conductor.workflow..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.conductor.analytics..");
    rule.check(workflowClasses);
  }
}
