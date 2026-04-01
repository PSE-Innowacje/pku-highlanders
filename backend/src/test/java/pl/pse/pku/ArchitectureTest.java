package pl.pse.pku;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArchitectureTest {

    private JavaClasses applicationClasses;

    @BeforeAll
    void importClasses() {
        // Locate the compiled main classes directory via the application class file location,
        // then walk up to target/classes to import the entire production classpath.
        String classesPath = PkuApplication.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath();
        applicationClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPath(classesPath);
    }

    // -------------------------------------------------------------------------
    // Naming conventions
    // -------------------------------------------------------------------------

    @Test
    void controllers_should_be_named_with_controller_suffix() {
        classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().haveSimpleNameEndingWith("Controller")
                .because("REST controllers must follow the *Controller naming convention")
                .check(applicationClasses);
    }

    @Test
    void services_should_be_named_with_service_suffix() {
        classes()
                .that().areAnnotatedWith("org.springframework.stereotype.Service")
                .should().haveSimpleNameEndingWith("Service")
                .because("Spring services must follow the *Service naming convention")
                .check(applicationClasses);
    }

    @Test
    void repositories_should_be_named_with_repository_suffix() {
        classes()
                .that().areInterfaces()
                .and().areAssignableTo("org.springframework.data.repository.Repository")
                .should().haveSimpleNameEndingWith("Repository")
                .because("Spring Data repositories must follow the *Repository naming convention")
                .check(applicationClasses);
    }

    @Test
    void exception_handlers_should_reside_in_exception_package() {
        classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestControllerAdvice")
                .or().areAnnotatedWith("org.springframework.web.bind.annotation.ControllerAdvice")
                .should().resideInAPackage("..exception..")
                .because("Global exception handlers belong in the exception package")
                .check(applicationClasses);
    }

    // -------------------------------------------------------------------------
    // Layer dependency rules
    // -------------------------------------------------------------------------

    @Test
    void controllers_should_not_depend_on_repositories() {
        noClasses()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().dependOnClassesThat()
                .areAssignableTo("org.springframework.data.repository.Repository")
                .because("Controllers must delegate persistence to the service layer, not access repositories directly")
                .check(applicationClasses);
    }

    @Test
    void services_should_not_depend_on_controllers() {
        noClasses()
                .that().areAnnotatedWith("org.springframework.stereotype.Service")
                .should().dependOnClassesThat()
                .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .because("Services must not depend on the web layer")
                .check(applicationClasses);
    }

    @Test
    void repositories_should_not_depend_on_services_or_controllers() {
        noClasses()
                .that().areInterfaces()
                .and().areAssignableTo("org.springframework.data.repository.Repository")
                .should().dependOnClassesThat()
                .areAnnotatedWith("org.springframework.stereotype.Service")
                .because("Repositories must not depend on the service layer")
                .check(applicationClasses);
    }

    @Test
    void entities_should_not_depend_on_services_or_controllers() {
        noClasses()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().dependOnClassesThat()
                .areAnnotatedWith("org.springframework.stereotype.Service")
                .orShould().dependOnClassesThat()
                .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .because("JPA entities must be independent of service and web layers")
                .check(applicationClasses);
    }

    // -------------------------------------------------------------------------
    // Spring annotation usage rules
    // -------------------------------------------------------------------------

    @Test
    void transactional_should_not_be_used_on_controllers() {
        noClasses()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .because("Transaction boundaries belong in the service layer, not in controllers")
                .check(applicationClasses);
    }

    @Test
    void entities_should_reside_in_feature_packages() {
        classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("pl.pse.pku.(*)..")
                .because("JPA entities should live in a domain feature package, not in the root package")
                .check(applicationClasses);
    }

    // -------------------------------------------------------------------------
    // No package cycles
    // -------------------------------------------------------------------------

    @Test
    void no_cycles_between_feature_packages() {
        slices()
                .matching("pl.pse.pku.(*)..")
                .should().beFreeOfCycles()
                .because("Feature packages must not have circular dependencies")
                .check(applicationClasses);
    }
}
