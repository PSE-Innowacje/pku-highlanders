package pl.pse.pku.contractortype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContractorTypeDto(
    Long id,
    @NotBlank @Size(max = 10) String symbol,
    @NotBlank @Size(max = 255) String name,
    boolean system
) {}
