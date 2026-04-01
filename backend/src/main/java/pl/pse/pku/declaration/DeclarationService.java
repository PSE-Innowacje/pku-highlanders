package pl.pse.pku.declaration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pse.pku.declarationtype.DeclarationType;
import pl.pse.pku.declarationtype.DeclarationTypeFieldDto;
import pl.pse.pku.exception.BusinessException;
import pl.pse.pku.exception.ResourceNotFoundException;
import pl.pse.pku.userassignment.UserContractorTypeAssignmentRepository;

@Service
@RequiredArgsConstructor
public class DeclarationService {

    private final DeclarationRepository declarationRepository;
    private final UserContractorTypeAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<DeclarationDto> getMyDeclarations(String keycloakUserId) {
        return declarationRepository.findByKeycloakUserIdOrderByCreatedAtDesc(keycloakUserId).stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public DeclarationDetailDto getDeclarationDetail(Long id, String keycloakUserId) {
        var declaration = findUserDeclaration(id, keycloakUserId);
        return toDetailDto(declaration);
    }

    @Transactional
    public DeclarationDetailDto saveDeclaration(Long id, String keycloakUserId, Map<String, String> fieldValues, String comment) {
        var declaration = findUserDeclaration(id, keycloakUserId);

        if (declaration.getStatus() != DeclarationStatus.NIE_ZLOZONE && declaration.getStatus() != DeclarationStatus.ROBOCZE) {
            throw new BusinessException("Oświadczenie w statusie '" + statusLabel(declaration.getStatus()) + "' nie może być edytowane");
        }

        declaration.setFieldValues(fieldValues != null ? fieldValues : Map.of());
        declaration.setComment(comment);
        declaration.setStatus(DeclarationStatus.ROBOCZE);
        return toDetailDto(declarationRepository.save(declaration));
    }

    @Transactional
    public Map<String, Object> submitDeclaration(Long id, String keycloakUserId) {
        var declaration = findUserDeclaration(id, keycloakUserId);

        if (declaration.getStatus() != DeclarationStatus.ROBOCZE) {
            throw new BusinessException("Tylko oświadczenia w statusie 'Robocze' mogą być wysłane");
        }

        // Validate required fields
        var requiredFields = declaration.getDeclarationType().getFields().stream()
            .filter(f -> f.isRequired())
            .toList();
        var values = declaration.getFieldValues();
        for (var field : requiredFields) {
            var value = values.get(field.getFieldCode());
            if (value == null || value.isBlank()) {
                throw new BusinessException("Pole '" + field.getFieldName() + "' (" + field.getFieldCode() + ") jest wymagane");
            }
        }

        declaration.setStatus(DeclarationStatus.ZLOZONE);
        declarationRepository.save(declaration);

        // Build JSON for download
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("declarationNumber", declaration.getDeclarationNumber());
        json.put("declarationTypeCode", declaration.getDeclarationType().getCode());
        json.put("declarationTypeName", declaration.getDeclarationType().getName());
        json.put("status", "Złożone");
        json.put("submittedAt", LocalDateTime.now().toString());

        Map<String, Object> fieldsJson = new LinkedHashMap<>();
        for (var field : declaration.getDeclarationType().getFields()) {
            var val = values.getOrDefault(field.getFieldCode(), "");
            fieldsJson.put(field.getFieldCode(), Map.of(
                "name", field.getFieldName(),
                "value", val,
                "unit", field.getUnit()
            ));
        }
        json.put("fields", fieldsJson);

        if (declaration.getDeclarationType().isHasComment() && declaration.getComment() != null) {
            json.put("comment", declaration.getComment());
        }

        return json;
    }

    @Transactional
    public List<DeclarationDto> generateDeclarations(String keycloakUserId) {
        var assignments = assignmentRepository.findAllByKeycloakUserId(keycloakUserId);
        if (assignments.isEmpty()) {
            throw new BusinessException("Nie masz przypisanego typu kontrahenta");
        }

        boolean anyDeclarationTypes = false;
        LocalDateTime now = LocalDateTime.now();

        for (var assignment : assignments) {
            var contractorType = assignment.getContractorType();
            var declarationTypes = contractorType.getDeclarationTypes();

            if (!declarationTypes.isEmpty()) {
                anyDeclarationTypes = true;
            }

            for (DeclarationType dt : declarationTypes) {
                if (!declarationRepository.existsByKeycloakUserIdAndDeclarationTypeId(keycloakUserId, dt.getId())) {
                    var declaration = new Declaration();
                    declaration.setDeclarationNumber(generateNumber(dt, contractorType.getSymbol()));
                    declaration.setStatus(DeclarationStatus.NIE_ZLOZONE);
                    declaration.setKeycloakUserId(keycloakUserId);
                    declaration.setDeclarationType(dt);
                    declaration.setCreatedAt(now);
                    declarationRepository.save(declaration);
                }
            }
        }

        if (!anyDeclarationTypes) {
            var symbols = assignments.stream()
                .map(a -> a.getContractorType().getSymbol())
                .toList();
            throw new BusinessException("Typy kontrahenta " + symbols + " nie mają przypisanych typów oświadczeń");
        }

        return declarationRepository.findByKeycloakUserIdOrderByCreatedAtDesc(keycloakUserId).stream()
            .map(this::toDto)
            .toList();
    }

    private Declaration findUserDeclaration(Long id, String keycloakUserId) {
        var declaration = declarationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono oświadczenia o id " + id));
        if (!declaration.getKeycloakUserId().equals(keycloakUserId)) {
            throw new BusinessException("Brak dostępu do tego oświadczenia");
        }
        return declaration;
    }

    private String generateNumber(DeclarationType dt, String contractorSymbol) {
        String feeType = dt.getCode().split("\\.")[0];
        int year = LocalDateTime.now().getYear();
        int month = LocalDateTime.now().getMonthValue();
        int subperiod = 1;
        int version = 1;
        int rand = ThreadLocalRandom.current().nextInt(100, 999);
        return String.format("OSW/%s/%s/%d/%02d/%02d/%02d/%03d",
            feeType, contractorSymbol, year, month, subperiod, version, rand);
    }

    private String statusLabel(DeclarationStatus status) {
        return switch (status) {
            case NIE_ZLOZONE -> "Nie złożone";
            case ROBOCZE -> "Robocze";
            case ZLOZONE -> "Złożone";
        };
    }

    private DeclarationDto toDto(Declaration d) {
        return new DeclarationDto(
            d.getId(), d.getDeclarationNumber(), d.getStatus().name(),
            statusLabel(d.getStatus()), d.getDeclarationType().getCode(),
            d.getDeclarationType().getName(), d.getCreatedAt());
    }

    private DeclarationDetailDto toDetailDto(Declaration d) {
        var fieldDtos = d.getDeclarationType().getFields().stream()
            .map(f -> new DeclarationTypeFieldDto(
                f.getPosition(), f.getFieldCode(), f.getDataType(),
                f.getFieldName(), f.isRequired(), f.getUnit()))
            .toList();
        return new DeclarationDetailDto(
            d.getId(), d.getDeclarationNumber(), d.getStatus().name(),
            statusLabel(d.getStatus()), d.getDeclarationType().getCode(),
            d.getDeclarationType().getName(), d.getDeclarationType().isHasComment(),
            d.getCreatedAt(), d.getFieldValues(), d.getComment(), fieldDtos);
    }
}
