package pl.pse.pku.declaration;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/declarations")
@RequiredArgsConstructor
public class DeclarationController {

    private final DeclarationService service;

    @PostMapping("/generate")
    public List<DeclarationDto> generateMyDeclarations(@AuthenticationPrincipal Jwt jwt) {
        return service.generateDeclarationsForUser(jwt.getSubject());
    }

    @GetMapping
    public List<DeclarationDto> getMyDeclarations(@AuthenticationPrincipal Jwt jwt) {
        return service.getMyDeclarations(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public DeclarationDetailDto getDeclarationDetail(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.getDeclarationDetail(id, jwt.getSubject());
    }

    @PutMapping("/{id}")
    public DeclarationDetailDto saveDeclaration(
            @PathVariable Long id,
            @RequestBody SaveDeclarationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return service.saveDeclaration(id, jwt.getSubject(), request.fieldValues(), request.comment());
    }

    @PostMapping("/{id}/submit")
    public Map<String, Object> submitDeclaration(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.submitDeclaration(id, jwt.getSubject());
    }
}
