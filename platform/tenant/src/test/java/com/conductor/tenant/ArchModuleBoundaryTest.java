package com.conductor.tenant;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.conductor", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchModuleBoundaryTest {

  @ArchTest
  public static final ArchRule layerDependenciesAreRespected =
      layeredArchitecture()
          .consideringAllDependencies()
          .layer("TenantService")
          .definedBy("com.conductor.tenant..")
          .optionalLayer("IdentityService")
          .definedBy("com.conductor.identity..")
          .optionalLayer("SharedAuth")
          .definedBy("com.conductor.shared.auth..")
          .optionalLayer("SharedSecurity")
          .definedBy("com.conductor.shared.security..")
          .optionalLayer("SharedMiddleware")
          .definedBy("com.conductor.shared.middleware.tenant..")

          // Boundary rules
          .whereLayer("IdentityService")
          .mayOnlyBeAccessedByLayers("IdentityService")
          .whereLayer("TenantService")
          .mayOnlyBeAccessedByLayers("TenantService", "IdentityService");

  @ArchTest
  public static final ArchRule noCyclicDependencies =
      slices().matching("com.conductor.(*)..").should().beFreeOfCycles();
}
