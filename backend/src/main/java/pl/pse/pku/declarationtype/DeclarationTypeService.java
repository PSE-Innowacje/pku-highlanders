package pl.pse.pku.declarationtype;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pse.pku.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class DeclarationTypeService {

    private final DeclarationTypeRepository repository;

    @Transactional(readOnly = true)
    public List<DeclarationTypeDto> findAll() {
        return repository.findAll().stream()
            .map(dt -> new DeclarationTypeDto(
                dt.getId(), dt.getCode(), dt.getName(), dt.getContractorTypes(),
                dt.isHasComment(), dt.getFields().size()))
            .toList();
    }

    @Transactional(readOnly = true)
    public DeclarationTypeDetailDto findByCode(String code) {
        var dt = repository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono typu oświadczenia: " + code));
        var fieldDtos = dt.getFields().stream()
            .map(f -> new DeclarationTypeFieldDto(
                f.getPosition(), f.getFieldCode(), f.getDataType(),
                f.getFieldName(), f.isRequired(), f.getUnit()))
            .toList();
        return new DeclarationTypeDetailDto(
            dt.getId(), dt.getCode(), dt.getName(), dt.getContractorTypes(),
            dt.isHasComment(), fieldDtos);
    }
}
