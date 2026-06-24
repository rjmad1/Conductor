package com.conductor.customer;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.conductor", importOptions = ImportOption.DoNotIncludeTests.class)
public class CustomerArchBoundaryTest {

    @ArchTest
    public static final ArchRule noAccessToIdentity = noClasses()
            .that().resideInAPackage("com.conductor.customer..")
            .should().dependOnClassesThat().resideInAPackage("com.conductor.identity..");

    @ArchTest
    public static final ArchRule noAccessToWorkflow = noClasses()
            .that().resideInAPackage("com.conductor.customer..")
            .should().dependOnClassesThat().resideInAPackage("com.conductor.workflow..");

    @ArchTest
    public static final ArchRule noAccessToIntegrations = noClasses()
            .that().resideInAPackage("com.conductor.customer..")
            .should().dependOnClassesThat().resideInAPackage("com.conductor.integrations..");

    @ArchTest
    public static final ArchRule noAccessToAnalytics = noClasses()
            .that().resideInAPackage("com.conductor.customer..")
            .should().dependOnClassesThat().resideInAPackage("com.conductor.analytics..");

    @ArchTest
    public static final ArchRule noAccessToAi = noClasses()
            .that().resideInAPackage("com.conductor.customer..")
            .should().dependOnClassesThat().resideInAPackage("com.conductor.ai..");
}
