package pl.pse.pku.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("pl.pse.pku");
    }

    // --- Naming conventions ---

    @Test
    void controllers_should_be_suffixed_with_Controller() {
        classes()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .check(importedClasses);
    }

    @Test
    void services_should_be_suffixed_with_Service() {
        classes()
            .that().areAnnotatedWith(org.springframework.stereotype.Service.class)
            .should().haveSimpleNameEndingWith("Service")
            .check(importedClasses);
    }

    @Test
    void repositories_should_be_interfaces() {
        classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().beInterfaces()
            .check(importedClasses);
    }

    @Test
    void entities_should_have_table_annotation() {
        classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().beAnnotatedWith(jakarta.persistence.Table.class)
            .check(importedClasses);
    }

    // --- Dependency rules ---

    @Test
    void controllers_should_not_depend_on_repositories() {
        noClasses()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .because("Controllers should delegate to services, not access repositories directly")
            .check(importedClasses);
    }

    @Test
    void keycloak_package_should_not_depend_on_domain() {
        noClasses()
            .that().resideInAPackage("pl.pse.pku.keycloak..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "pl.pse.pku.declaration..",
                "pl.pse.pku.declarationtype..",
                "pl.pse.pku.contractortype..",
                "pl.pse.pku.contractordata..",
                "pl.pse.pku.userassignment..")
            .because("Keycloak integration should be independent of domain packages")
            .check(importedClasses);
    }

    @Test
    void config_package_should_not_depend_on_domain_services() {
        noClasses()
            .that().resideInAPackage("pl.pse.pku.config..")
            .should().dependOnClassesThat().areAnnotatedWith(org.springframework.stereotype.Service.class)
            .because("Configuration should not depend on service layer")
            .check(importedClasses);
    }

    // --- General coding rules ---

    @Test
    void no_classes_should_access_standard_streams() {
        noClasses()
            .should().accessClassesThat().belongToAnyOf(System.class)
            .orShould().callMethod(java.io.PrintStream.class, "println", String.class)
            .because("Use SLF4J logging instead of System.out/System.err")
            .check(importedClasses);
    }

    @Test
    void no_field_injection() {
        noClasses()
            .should().beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
            .because("Use constructor injection (via @RequiredArgsConstructor) instead of field injection")
            .check(importedClasses);
    }

    // --- DTO rules ---

    @Test
    void dtos_should_be_records() {
        classes()
            .that().haveSimpleNameEndingWith("Dto")
            .should().beRecords()
            .because("DTOs should be immutable records")
            .check(importedClasses);
    }

    @Test
    void request_objects_should_be_records() {
        classes()
            .that().haveSimpleNameEndingWith("Request")
            .should().beRecords()
            .because("Request objects should be immutable records")
            .check(importedClasses);
    }

    // --- Package structure ---

    @Test
    void exception_classes_should_reside_in_exception_package() {
        classes()
            .that().areAssignableTo(RuntimeException.class)
            .and().resideInAPackage("pl.pse.pku..")
            .should().resideInAPackage("pl.pse.pku.exception..")
            .check(importedClasses);
    }

    @Test
    void seeders_should_implement_application_runner() {
        classes()
            .that().haveSimpleNameEndingWith("Seeder")
            .should().implement(org.springframework.boot.ApplicationRunner.class)
            .check(importedClasses);
    }
}
