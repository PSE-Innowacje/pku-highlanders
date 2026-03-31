package pl.pse.pku.declaration;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeclarationRepository extends JpaRepository<Declaration, Long> {
    List<Declaration> findByKeycloakUserIdOrderByCreatedAtDesc(String keycloakUserId);
    boolean existsByKeycloakUserIdAndDeclarationTypeId(String keycloakUserId, Long declarationTypeId);
}
