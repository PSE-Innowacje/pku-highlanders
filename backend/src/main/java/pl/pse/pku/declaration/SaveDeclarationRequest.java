package pl.pse.pku.declaration;

import java.util.Map;

public record SaveDeclarationRequest(
    Map<String, String> fieldValues,
    String comment
) {}
