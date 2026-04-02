package pl.pse.pku.declaration;

import java.time.LocalDateTime;

public record DeclarationDto(
    Long id,
    String declarationNumber,
    String status,
    String statusLabel,
    String declarationTypeCode,
    String declarationTypeName,
    LocalDateTime createdAt
) {}
