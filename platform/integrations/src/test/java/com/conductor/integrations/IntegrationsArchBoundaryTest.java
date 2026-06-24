package com.conductor.integrations;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.conductor", importOptions = ImportOption.DoNotIncludeTests.class)
public class IntegrationsArchBoundaryTest {

    @ArchTest
    public static final ArchRule integrationsLayerBoundaries = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Integrations").definedBy("com.conductor.integrations..")
            .optionalLayer("Workflow").definedBy("com.conductor.workflow..")
            .optionalLayer("Customer").definedBy("com.conductor.customer..")
            
            .whereLayer("Workflow").mayOnlyBeAccessedByLayers("Workflow")
            .whereLayer("Customer").mayOnlyBeAccessedByLayers("Customer");

    @ArchTest
    public static final ArchRule noCycles = slices()
            .matching("com.conductor.(*)..")
            .should().beFreeOfCycles();
}
