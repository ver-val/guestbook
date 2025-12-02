package com.example.guestbook.core;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.guestbook.core")
class ArchitectureTest {

    @ArchTest
    static final ArchRule core_should_not_depend_on_servlet_or_jdbc =
            noClasses().should().dependOnClassesThat()
                    .resideInAnyPackage("jakarta.servlet..", "javax.servlet..", "java.sql..", "jakarta.sql..");

    @ArchTest
    static final ArchRule core_should_not_depend_on_other_modules =
            noClasses().should().dependOnClassesThat()
                    .resideInAnyPackage("..web..", "..persistence..");
}
