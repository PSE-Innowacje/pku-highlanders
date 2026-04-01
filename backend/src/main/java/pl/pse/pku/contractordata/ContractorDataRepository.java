package pl.pse.pku.contractordata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractorDataRepository extends JpaRepository<ContractorData, Long> {
    Optional<ContractorData> findByKeycloakUserId(String keycloakUserId);
    List<ContractorData> findByKeycloakUserIdIn(List<String> keycloakUserIds);
}
