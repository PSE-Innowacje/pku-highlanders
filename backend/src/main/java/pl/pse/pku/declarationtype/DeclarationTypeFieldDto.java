package pl.pse.pku.declarationtype;

public record DeclarationTypeFieldDto(
    String position,
    String fieldCode,
    String dataType,
    String fieldName,
    boolean required,
    String unit
) {}
