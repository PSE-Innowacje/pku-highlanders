package pl.pse.pku.keycloak;

import java.util.List;

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
            .uri(serverUrl + "/admin/realms/{realm}/roles/Kontrahent/users", realm)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
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
