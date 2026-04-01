package pl.pse.pku.userassignment;

import pl.pse.pku.contractordata.ContractorDataDto;
import pl.pse.pku.contractortype.ContractorTypeDto;

public record CurrentUserContractorDto(
    String firstName,
    String lastName,
    String agreementNumber,
    String contractorFullName,
    String contractorAbbreviation,
    ContractorTypeDto assignedType,
    ContractorDataDto contractorData
) {}
