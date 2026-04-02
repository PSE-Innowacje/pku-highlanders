package pl.pse.pku;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import pl.pse.pku.keycloak.KeycloakAdminService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Base class for integration tests.
 *
 * Uses a singleton PostgreSQL Testcontainer (started once for the entire test run via the
 * static initializer) so all subclasses share the same container and the same Spring
 * application context. MockMvc is wired with Spring Security's jwt() post-processor to
 * bypass Keycloak token validation entirely. KeycloakAdminService is mocked to avoid HTTP
 * calls to the Keycloak Admin API. Each test runs in a rolled-back transaction.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
public abstract class AbstractIT {

    // Singleton container: started once for the whole test suite so every subclass
    // shares the same JDBC URL and therefore the same cached Spring context.
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("pku")
        .withUsername("pku")
        .withPassword("pku");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Provide a dummy JWK-set URI so NimbusJwtDecoder is configured without HTTP calls at startup.
        // The jwt() post-processor bypasses token validation entirely during tests.
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
            () -> "http://localhost:0/jwks");
    }

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    protected ObjectMapper objectMapper;

    // Mocked so that tests don't need a running Keycloak Admin API
    @MockitoBean
    protected KeycloakAdminService keycloakAdminService;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(springSecurity())
            .build();
    }

    protected static org.springframework.test.web.servlet.request.RequestPostProcessor adminJwt() {
        return jwt()
            .jwt(b -> b
                .subject("admin-user-id")
                .claim("realm_access", Map.of("roles", List.of("Administrator"))))
            .authorities(new SimpleGrantedAuthority("ROLE_Administrator"));
    }

    protected static org.springframework.test.web.servlet.request.RequestPostProcessor userJwt(String userId) {
        return jwt()
            .jwt(b -> b
                .subject(userId)
                .claim("realm_access", Map.of("roles", List.of("Kontrahent"))))
            .authorities(new SimpleGrantedAuthority("ROLE_Kontrahent"));
    }
}
