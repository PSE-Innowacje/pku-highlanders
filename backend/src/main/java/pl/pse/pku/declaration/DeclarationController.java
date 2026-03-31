package pl.pse.pku.declaration;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/declarations")
@RequiredArgsConstructor
public class DeclarationController {

    private final DeclarationService service;

    @GetMapping
    public List<DeclarationDto> getMyDeclarations(@AuthenticationPrincipal Jwt jwt) {
        return service.getMyDeclarations(jwt.getSubject());
    }

    @PostMapping("/generate")
    public List<DeclarationDto> generateDeclarations(@AuthenticationPrincipal Jwt jwt) {
        return service.generateDeclarations(jwt.getSubject());
    }
}
