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
import pl.pse.pku.contractordata.ContractorData;
import pl.pse.pku.contractordata.ContractorDataRepository;
import pl.pse.pku.declarationtype.DeclarationType;
import pl.pse.pku.declarationtype.DeclarationTypeField;
import pl.pse.pku.declarationtype.DeclarationTypeFieldDto;
import pl.pse.pku.exception.BusinessException;
import pl.pse.pku.exception.ResourceNotFoundException;
import pl.pse.pku.keycloak.KeycloakAdminService;
import pl.pse.pku.userassignment.UserContractorTypeAssignmentRepository;

@Service
@RequiredArgsConstructor
public class DeclarationService {

    private final DeclarationRepository declarationRepository;
    private final UserContractorTypeAssignmentRepository assignmentRepository;
    private final ContractorDataRepository contractorDataRepository;
    private final KeycloakAdminService keycloakAdminService;

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

        if (fieldValues != null) {
            for (var field : declaration.getDeclarationType().getFields()) {
                var value = fieldValues.get(field.getFieldCode());
                if (value != null && !value.isBlank()) {
                    validateFieldValue(field, value);
                }
            }
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
        var allFields = declaration.getDeclarationType().getFields();
        var values = declaration.getFieldValues();
        for (var field : allFields) {
            var value = values.get(field.getFieldCode());
            if (field.isRequired() && (value == null || value.isBlank())) {
                throw new BusinessException("Pole '" + field.getFieldName() + "' (" + field.getFieldCode() + ") jest wymagane");
            }
            if (value != null && !value.isBlank()) {
                validateFieldValue(field, value);
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

        // Contractor data
        var assignment = assignmentRepository.findByKeycloakUserId(keycloakUserId).orElse(null);
        var contractorData = contractorDataRepository.findByKeycloakUserId(keycloakUserId).orElse(null);
        Map<String, Object> contractorJson = new LinkedHashMap<>();
        if (assignment != null) {
            contractorJson.put("contractorType", assignment.getContractorType().getSymbol());
            contractorJson.put("contractorTypeName", assignment.getContractorType().getName());
        }
        if (contractorData != null) {
            contractorJson.put("contractorAbbreviation", contractorData.getContractorAbbreviation());
            contractorJson.put("contractorFullName", contractorData.getContractorFullName());
            contractorJson.put("contractorShortName", contractorData.getContractorShortName());
            contractorJson.put("krs", contractorData.getKrs());
            contractorJson.put("nip", contractorData.getNip());
            contractorJson.put("registeredAddress", contractorData.getRegisteredAddress());
            contractorJson.put("agreementNumber", contractorData.getAgreementNumber());
            contractorJson.put("agreementDateFrom", contractorData.getAgreementDateFrom() != null ? contractorData.getAgreementDateFrom().toString() : null);
            contractorJson.put("agreementDateTo", contractorData.getAgreementDateTo() != null ? contractorData.getAgreementDateTo().toString() : null);
        }
        json.put("contractor", contractorJson);

        // Submitter info
        try {
            var user = keycloakAdminService.getKontrahentUsers().stream()
                .filter(u -> u.id().equals(keycloakUserId))
                .findFirst().orElse(null);
            if (user != null) {
                json.put("submitter", Map.of(
                    "firstName", user.firstName() != null ? user.firstName() : "",
                    "lastName", user.lastName() != null ? user.lastName() : "",
                    "email", user.email() != null ? user.email() : ""));
            }
        } catch (Exception ignored) {
            // Keycloak lookup failure should not block submission
        }

        Map<String, Object> fieldsJson = new LinkedHashMap<>();
        for (var field : allFields) {
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
        var assignment = assignmentRepository.findByKeycloakUserId(keycloakUserId)
            .orElseThrow(() -> new BusinessException("Nie masz przypisanego typu kontrahenta"));

        var contractorType = assignment.getContractorType();
        var declarationTypes = contractorType.getDeclarationTypes();

        if (declarationTypes.isEmpty()) {
            throw new BusinessException("Typ kontrahenta '" + contractorType.getSymbol() + "' nie ma przypisanych typów oświadczeń");
        }

        LocalDateTime now = LocalDateTime.now();
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

    private void validateFieldValue(DeclarationTypeField field, String value) {
        var matcher = java.util.regex.Pattern.compile("Number\\s*\\((\\d+),(\\d+)\\)").matcher(field.getDataType());
        if (matcher.find()) {
            int maxIntDigits = Integer.parseInt(matcher.group(1));
            int maxDecDigits = Integer.parseInt(matcher.group(2));
            String[] parts = value.split("\\.");
            String intPart = parts[0].replaceFirst("^-?0*", "");
            if (intPart.isEmpty()) intPart = "0";
            if (intPart.length() > maxIntDigits) {
                throw new BusinessException("Pole '" + field.getFieldName() + "' (" + field.getFieldCode()
                    + ") — część całkowita nie może przekraczać " + maxIntDigits + " cyfr");
            }
            if (parts.length > 1 && parts[1].length() > maxDecDigits) {
                throw new BusinessException("Pole '" + field.getFieldName() + "' (" + field.getFieldCode()
                    + ") — część dziesiętna nie może przekraczać " + maxDecDigits + " cyfr");
            }
        }
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
