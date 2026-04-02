package pl.pse.pku.contractortype;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/contractor-types")
@RequiredArgsConstructor
public class ContractorTypeController {

    private final ContractorTypeService service;

    @GetMapping
    public List<ContractorTypeWithDeclarationsDto> findAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractorTypeDto create(@Valid @RequestBody ContractorTypeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ContractorTypeDto update(@PathVariable Long id, @Valid @RequestBody ContractorTypeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/declaration-types")
    public ContractorTypeWithDeclarationsDto updateDeclarationTypes(
            @PathVariable Long id,
            @RequestBody UpdateDeclarationTypesRequest request) {
        return service.updateDeclarationTypes(id, request.declarationTypeIds());
    }
}
