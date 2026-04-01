package pl.pse.pku.keycloak;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class KeycloakAdminService {

    private final RestClient restClient;
    private final String serverUrl;
    private final String adminUsername;
    private final String adminPassword;
    private final String realm;

    public KeycloakAdminService(
            @Value("${keycloak.admin.server-url}") String serverUrl,
            @Value("${keycloak.admin.username}") String adminUsername,
            @Value("${keycloak.admin.password}") String adminPassword,
            @Value("${keycloak.admin.realm}") String realm) {
        this.serverUrl = serverUrl;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.realm = realm;
        this.restClient = RestClient.create();
    }

    public List<KeycloakUserDto> getKontrahentUsers() {
        String token = getAdminToken();
        return restClient.get()
            .uri(serverUrl + "/admin/realms/{realm}/roles/Kontrahent/users?max=1000", realm)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    public String createUser(String username, String email, String firstName, String lastName, String password) {
        String token = getAdminToken();

        Map<String, Object> userRep = Map.of(
            "username", username,
            "email", email,
            "emailVerified", true,
            "enabled", true,
            "firstName", firstName,
            "lastName", lastName,
            "credentials", List.of(Map.of(
                "type", "password",
                "value", password,
                "temporary", false
            ))
        );

        restClient.post()
            .uri(serverUrl + "/admin/realms/{realm}/users", realm)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(userRep)
            .retrieve()
            .toBodilessEntity();

        // Fetch the created user to get their ID
        List<KeycloakUserDto> found = restClient.get()
            .uri(serverUrl + "/admin/realms/{realm}/users?username={username}&exact=true", realm, username)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        if (found == null || found.isEmpty()) {
            throw new RuntimeException("Failed to find created user: " + username);
        }
        return found.getFirst().id();
    }

    public void assignRealmRole(String userId, String roleName) {
        String token = getAdminToken();

        // Get the role representation
        record RoleRep(String id, String name) {}
        var role = restClient.get()
            .uri(serverUrl + "/admin/realms/{realm}/roles/{roleName}", realm, roleName)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(RoleRep.class);

        // Assign the role
        restClient.post()
            .uri(serverUrl + "/admin/realms/{realm}/users/{userId}/role-mappings/realm", realm, userId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(List.of(Map.of("id", role.id(), "name", role.name())))
            .retrieve()
            .toBodilessEntity();
    }

    public boolean userExists(String username) {
        String token = getAdminToken();
        List<KeycloakUserDto> found = restClient.get()
            .uri(serverUrl + "/admin/realms/{realm}/users?username={username}&exact=true", realm, username)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
        return found != null && !found.isEmpty();
    }

    private String getAdminToken() {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("grant_type", "password");
        formData.add("client_id", "admin-cli");
        formData.add("username", adminUsername);
        formData.add("password", adminPassword);

        record TokenResponse(String access_token) {}

        var response = restClient.post()
            .uri(serverUrl + "/realms/master/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(formData)
            .retrieve()
            .body(TokenResponse.class);

        return response.access_token();
    }
}
