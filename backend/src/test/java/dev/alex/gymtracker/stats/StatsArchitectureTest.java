package dev.alex.gymtracker.stats;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "dev.alex.gymtracker",
                importOptions = ImportOption.DoNotIncludeTests.class)
class StatsArchitectureTest {

    @ArchTest
    static final ArchRule statsIsIsolated =
        noClasses().that().resideInAPackage("..gymtracker.stats..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..gymtracker.workout..",
                "..gymtracker.exercise..",
                "..gymtracker.template..");
}
