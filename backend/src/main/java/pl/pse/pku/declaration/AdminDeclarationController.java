package pl.pse.pku.declaration;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.pse.pku.declarationtype.GenerateRequest;

@RestController
@RequestMapping("/api/admin/declaration-types")
@RequiredArgsConstructor
public class AdminDeclarationController {

    private final DeclarationService declarationService;

    @PostMapping("/{code}/generate")
    public Map<String, Object> generateDeclarations(
            @PathVariable String code,
            @RequestBody GenerateRequest request) {
        int count = declarationService.generateDeclarationsForSchedule(code, request.scheduleDay());
        return Map.of("generated", count);
    }
}
