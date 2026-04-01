package pl.pse.pku.contractortype;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractorTypeRepository extends JpaRepository<ContractorType, Long> {
    boolean existsBySymbol(String symbol);
    boolean existsBySymbolAndIdNot(String symbol, Long id);
    Optional<ContractorType> findBySymbolIgnoreCase(String symbol);

    @Query("SELECT ct FROM ContractorType ct JOIN ct.declarationTypes dt WHERE dt.id = :declarationTypeId")
    List<ContractorType> findByDeclarationTypeId(@Param("declarationTypeId") Long declarationTypeId);
}
