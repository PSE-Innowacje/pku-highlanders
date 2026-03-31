package pl.pse.pku.userassignment;

import pl.pse.pku.contractortype.ContractorTypeDto;

public record KontrahentUserWithTypesDto(
    String keycloakUserId,
    String username,
    String firstName,
    String lastName,
    String email,
    ContractorTypeDto assignedType
) {}
