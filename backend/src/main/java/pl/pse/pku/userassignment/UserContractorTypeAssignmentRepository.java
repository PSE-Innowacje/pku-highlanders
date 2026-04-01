package pl.pse.pku.userassignment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserContractorTypeAssignmentRepository extends JpaRepository<UserContractorTypeAssignment, Long> {
    Optional<UserContractorTypeAssignment> findByKeycloakUserId(String keycloakUserId);
    List<UserContractorTypeAssignment> findByKeycloakUserIdIn(List<String> keycloakUserIds);
    List<UserContractorTypeAssignment> findAllByKeycloakUserId(String keycloakUserId);
    void deleteByKeycloakUserId(String keycloakUserId);
    void deleteAllByKeycloakUserId(String keycloakUserId);
}
