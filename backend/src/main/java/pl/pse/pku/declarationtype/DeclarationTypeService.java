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
        var dt = findByCodeOrThrow(code);
        var fieldDtos = dt.getFields().stream()
            .map(f -> new DeclarationTypeFieldDto(
                f.getPosition(), f.getFieldCode(), f.getDataType(),
                f.getFieldName(), f.isRequired(), f.getUnit()))
            .toList();
        return new DeclarationTypeDetailDto(
            dt.getId(), dt.getCode(), dt.getName(), dt.getContractorTypes(),
            dt.isHasComment(), fieldDtos);
    }

    @Transactional(readOnly = true)
    public List<ScheduleEntryDto> getScheduleEntries(String code) {
        var dt = findByCodeOrThrow(code);
        return dt.getScheduleEntries().stream()
            .map(e -> new ScheduleEntryDto(e.getId(), e.getPosition(), e.getDay(), e.getHour(), e.getDayType()))
            .toList();
    }

    @Transactional
    public List<ScheduleEntryDto> saveScheduleEntries(String code, List<ScheduleEntryDto> entries) {
        var dt = findByCodeOrThrow(code);
        dt.getScheduleEntries().clear();
        for (var entry : entries) {
            var e = new ScheduleEntry();
            e.setDeclarationType(dt);
            e.setPosition(entry.position());
            e.setDay(entry.day());
            e.setHour(entry.hour());
            e.setDayType(entry.dayType());
            dt.getScheduleEntries().add(e);
        }
        repository.save(dt);
        return dt.getScheduleEntries().stream()
            .map(e -> new ScheduleEntryDto(e.getId(), e.getPosition(), e.getDay(), e.getHour(), e.getDayType()))
            .toList();
    }

    private DeclarationType findByCodeOrThrow(String code) {
        return repository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono typu oświadczenia: " + code));
    }
}
