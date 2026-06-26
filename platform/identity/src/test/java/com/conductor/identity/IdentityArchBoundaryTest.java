package com.conductor.identity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.conductor.identity",
    importOptions = ImportOption.DoNotIncludeTests.class)
public class IdentityArchBoundaryTest {

  @ArchTest
  public static final ArchRule identityMustNotDependOnOtherPlatformModules =
      noClasses()
          .that()
          .resideInAPackage("com.conductor.identity..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              // Note: identity→tenant is an accepted dependency (UserService uses
              // KeycloakAdminService for role assignment).
              // TODO(CC-2): extract a shared interface so identity need not depend on
              // platform:tenant.
              "com.conductor.workflow..",
              "com.conductor.customer..",
              "com.conductor.integrations..",
              "com.conductor.events..")
          .because("platform modules must not depend on each other; use shared interfaces");
}
