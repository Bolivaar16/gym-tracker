package dev.alex.gymtracker.coach;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "dev.alex.gymtracker",
                importOptions = ImportOption.DoNotIncludeTests.class)
class CoachArchitectureTest {

    @ArchTest
    static final ArchRule coachIsIsolated =
        noClasses().that().resideInAPackage("..gymtracker.coach..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..gymtracker.workout..",
                "..gymtracker.exercise..",
                "..gymtracker.template..",
                "..gymtracker.stats..");
}
