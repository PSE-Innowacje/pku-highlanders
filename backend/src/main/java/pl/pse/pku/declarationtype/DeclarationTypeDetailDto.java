package pl.pse.pku.declarationtype;

import java.util.List;

public record DeclarationTypeDetailDto(
    Long id,
    String code,
    String name,
    String contractorTypes,
    boolean hasComment,
    List<DeclarationTypeFieldDto> fields
) {}
