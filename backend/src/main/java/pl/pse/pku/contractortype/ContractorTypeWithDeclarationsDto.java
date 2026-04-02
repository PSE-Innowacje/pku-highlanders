package pl.pse.pku.contractortype;

import java.util.List;

public record ContractorTypeWithDeclarationsDto(
    Long id,
    String symbol,
    String name,
    boolean system,
    List<DeclarationTypeRefDto> declarationTypes
) {}
