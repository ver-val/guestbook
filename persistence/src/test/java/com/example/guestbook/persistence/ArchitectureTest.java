package com.example.guestbook.persistence;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.guestbook.persistence")
class ArchitectureTest {

    @ArchTest
    static final ArchRule persistence_should_not_depend_on_web =
            noClasses().should().dependOnClassesThat().resideInAPackage("..web..");

    @ArchTest
    static final ArchRule persistence_should_not_depend_on_servlet =
            noClasses().should().dependOnClassesThat().resideInAnyPackage("jakarta.servlet..", "javax.servlet..");

    @ArchTest
    static final ArchRule repositories_should_live_in_persistence =
            classes().that().haveSimpleNameEndingWith("Repository")
                    .should().resideInAPackage("..persistence..");
}
