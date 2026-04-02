package pl.pse.pku.contractortype;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pse.pku.declarationtype.DeclarationTypeRepository;
import pl.pse.pku.exception.BusinessException;
import pl.pse.pku.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ContractorTypeService {

    private final ContractorTypeRepository repository;
    private final DeclarationTypeRepository declarationTypeRepository;

    @Transactional(readOnly = true)
    public List<ContractorTypeWithDeclarationsDto> findAll() {
        return repository.findAll().stream()
            .map(this::toFullDto)
            .toList();
    }

    @Transactional
    public ContractorTypeDto create(ContractorTypeRequest request) {
        if (repository.existsBySymbol(request.symbol())) {
            throw new BusinessException("Typ kontrahenta o symbolu '" + request.symbol() + "' już istnieje");
        }
        var entity = new ContractorType(null, request.symbol(), request.name(), false);
        return toDto(repository.save(entity));
    }

    @Transactional
    public ContractorTypeDto update(Long id, ContractorTypeRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono typu kontrahenta o id " + id));
        if (entity.isSystem()) {
            throw new BusinessException("Nie można edytować systemowego typu kontrahenta");
        }
        if (repository.existsBySymbolAndIdNot(request.symbol(), id)) {
            throw new BusinessException("Typ kontrahenta o symbolu '" + request.symbol() + "' już istnieje");
        }
        entity.setSymbol(request.symbol());
        entity.setName(request.name());
        return toDto(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono typu kontrahenta o id " + id));
        if (entity.isSystem()) {
            throw new BusinessException("Nie można usunąć systemowego typu kontrahenta");
        }
        repository.delete(entity);
    }

    @Transactional
    public ContractorTypeWithDeclarationsDto updateDeclarationTypes(Long id, List<Long> declarationTypeIds) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono typu kontrahenta o id " + id));

        var declarationTypes = declarationTypeRepository.findAllById(declarationTypeIds);
        if (declarationTypes.size() != declarationTypeIds.size()) {
            throw new ResourceNotFoundException("Niektóre typy oświadczeń nie zostały znalezione");
        }

        entity.getDeclarationTypes().clear();
        entity.getDeclarationTypes().addAll(declarationTypes);
        return toFullDto(repository.save(entity));
    }

    private ContractorTypeDto toDto(ContractorType entity) {
        return new ContractorTypeDto(entity.getId(), entity.getSymbol(), entity.getName(), entity.isSystem());
    }

    private ContractorTypeWithDeclarationsDto toFullDto(ContractorType entity) {
        var declRefs = entity.getDeclarationTypes().stream()
            .map(dt -> new DeclarationTypeRefDto(dt.getId(), dt.getCode(), dt.getName()))
            .toList();
        return new ContractorTypeWithDeclarationsDto(
            entity.getId(), entity.getSymbol(), entity.getName(), entity.isSystem(), declRefs);
    }
}
