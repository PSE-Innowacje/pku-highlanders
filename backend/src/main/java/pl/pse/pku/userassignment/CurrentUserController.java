package pl.pse.pku.userassignment;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class CurrentUserController {

    private final UserAssignmentService service;

    @GetMapping("/contractor-data")
    public CurrentUserContractorDto getMyContractorData(JwtAuthenticationToken auth) {
        String keycloakUserId = auth.getToken().getSubject();
        return service.getCurrentUserContractorData(keycloakUserId);
    }
}
