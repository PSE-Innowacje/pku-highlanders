package pl.pse.pku.keycloak;

public record KeycloakUserDto(
    String id,
    String username,
    String firstName,
    String lastName,
    String email
) {}
