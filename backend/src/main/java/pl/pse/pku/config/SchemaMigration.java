package pl.pse.pku.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class SchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        dropUniqueConstraintOnKeycloakUserId();
    }

    private void dropUniqueConstraintOnKeycloakUserId() {
        try {
            var constraints = jdbcTemplate.queryForList(
                "SELECT constraint_name FROM information_schema.table_constraints " +
                "WHERE table_name = 'user_contractor_type_assignments' " +
                "AND constraint_type = 'UNIQUE' " +
                "AND constraint_name LIKE '%keycloak_user_id%'");

            for (var row : constraints) {
                String name = (String) row.get("constraint_name");
                jdbcTemplate.execute("ALTER TABLE user_contractor_type_assignments DROP CONSTRAINT " + name);
                log.info("Dropped unique constraint {} to allow multi-type assignments", name);
            }
        } catch (Exception e) {
            log.debug("No unique constraint to drop on keycloak_user_id: {}", e.getMessage());
        }
    }
}
