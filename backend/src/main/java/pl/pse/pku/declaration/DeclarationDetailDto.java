package pl.pse.pku.declaration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import pl.pse.pku.declarationtype.DeclarationTypeFieldDto;

public record DeclarationDetailDto(
    Long id,
    String declarationNumber,
    String status,
    String statusLabel,
    String declarationTypeCode,
    String declarationTypeName,
    boolean hasComment,
    LocalDateTime createdAt,
    Map<String, String> fieldValues,
    String comment,
    List<DeclarationTypeFieldDto> fields
) {}
