package pl.pse.pku.contractortype;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractorTypeRepository extends JpaRepository<ContractorType, Long> {
    boolean existsBySymbol(String symbol);
    boolean existsBySymbolAndIdNot(String symbol, Long id);
    Optional<ContractorType> findBySymbolIgnoreCase(String symbol);
}
