package com.conductor.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ArchitectureTest {

  private static JavaClasses classes;

  @BeforeAll
  static void setUp() {
    classes =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.conductor");
  }

  @Test
  void testNoCyclicDependenciesBetweenSlices() {
    slices().matching("com.conductor.(*)..").should().beFreeOfCycles().check(classes);
  }

  @Test
  void testSharedModulesDoNotDependOnPlatformModules() {
    noClasses()
        .that()
        .resideInAPackage("com.conductor.shared..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("com.conductor.platform..")
        .check(classes);
  }
}
