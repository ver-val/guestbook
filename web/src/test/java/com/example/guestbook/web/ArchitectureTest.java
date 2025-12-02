package com.example.guestbook.web;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.example.guestbook")
class ArchitectureTest {

    @ArchTest
    static final ArchRule web_should_not_depend_on_persistence =
            noClasses().that().resideInAPackage("..web..")
                    .should().dependOnClassesThat().resideInAPackage("..persistence..");

    @ArchTest
    static final ArchRule controllers_should_live_in_web =
            classes().that().areAnnotatedWith(WebServlet.class)
                    .or().areAssignableTo(HttpServlet.class)
                    .should().resideInAPackage("..web..");
}
