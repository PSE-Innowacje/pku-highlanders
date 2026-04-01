package pl.pse.pku.contractordata;

import java.time.LocalDate;

public record ContractorDataDto(
    String contractorAbbreviation,
    String contractorFullName,
    String contractorShortName,
    String krs,
    String nip,
    String registeredAddress,
    String contractorCode,
    String agreementNumber,
    LocalDate agreementDateFrom,
    LocalDate agreementDateTo
) {}
