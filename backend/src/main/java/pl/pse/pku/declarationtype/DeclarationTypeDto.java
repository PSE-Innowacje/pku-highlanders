package pl.pse.pku.declarationtype;

public record DeclarationTypeDto(
    Long id,
    String code,
    String name,
    String contractorTypes,
    boolean hasComment,
    int fieldCount
) {}
