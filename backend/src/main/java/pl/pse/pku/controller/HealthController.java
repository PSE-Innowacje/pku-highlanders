package pl.pse.pku.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "sub", jwt.getSubject(),
            "email", jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : "",
            "name", jwt.getClaimAsString("preferred_username") != null ? jwt.getClaimAsString("preferred_username") : "",
            "roles", jwt.getClaimAsMap("realm_access") != null ? jwt.getClaimAsMap("realm_access").get("roles") : java.util.List.of()
        );
    }
}
