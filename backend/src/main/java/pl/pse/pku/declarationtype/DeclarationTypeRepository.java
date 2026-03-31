package pl.pse.pku.declarationtype;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeclarationTypeRepository extends JpaRepository<DeclarationType, Long> {
    Optional<DeclarationType> findByCode(String code);
}
