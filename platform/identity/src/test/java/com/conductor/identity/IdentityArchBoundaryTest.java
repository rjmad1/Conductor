package com.conductor.identity;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.conductor.identity", importOptions = ImportOption.DoNotIncludeTests.class)
public class IdentityArchBoundaryTest {

    @ArchTest
    public static final ArchRule identityMustNotDependOnOtherPlatformModules =
            noClasses()
                    .that().resideInAPackage("com.conductor.identity..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.conductor.tenant..",
                            "com.conductor.workflow..",
                            "com.conductor.customer..",
                            "com.conductor.integrations..",
                            "com.conductor.events.."
                    )
                    .because("platform modules must not depend on each other; use shared interfaces");
}
