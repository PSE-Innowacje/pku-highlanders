package pl.pse.pku.declarationtype;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/declaration-types")
@RequiredArgsConstructor
public class DeclarationTypeController {

    private final DeclarationTypeService service;

    @GetMapping
    public List<DeclarationTypeDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{code}")
    public DeclarationTypeDetailDto findByCode(@PathVariable String code) {
        return service.findByCode(code);
    }
}
