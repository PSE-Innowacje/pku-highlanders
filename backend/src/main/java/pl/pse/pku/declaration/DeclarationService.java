package pl.pse.pku.declaration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pse.pku.declarationtype.DeclarationType;
import pl.pse.pku.exception.BusinessException;
import pl.pse.pku.userassignment.UserContractorTypeAssignment;
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

    @Transactional
    public List<DeclarationDto> generateDeclarations(String keycloakUserId) {
        var assignment = assignmentRepository.findByKeycloakUserId(keycloakUserId)
            .orElseThrow(() -> new BusinessException("Nie masz przypisanego typu kontrahenta"));

        var contractorType = assignment.getContractorType();
        var declarationTypes = contractorType.getDeclarationTypes();

        if (declarationTypes.isEmpty()) {
            throw new BusinessException("Typ kontrahenta '" + contractorType.getSymbol() + "' nie ma przypisanych typów oświadczeń");
        }

        List<Declaration> created = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (DeclarationType dt : declarationTypes) {
            if (!declarationRepository.existsByKeycloakUserIdAndDeclarationTypeId(keycloakUserId, dt.getId())) {
                var declaration = new Declaration();
                declaration.setDeclarationNumber(generateNumber(dt, contractorType.getSymbol()));
                declaration.setStatus(DeclarationStatus.NIE_ZLOZONE);
                declaration.setKeycloakUserId(keycloakUserId);
                declaration.setDeclarationType(dt);
                declaration.setCreatedAt(now);
                created.add(declarationRepository.save(declaration));
            }
        }

        return declarationRepository.findByKeycloakUserIdOrderByCreatedAtDesc(keycloakUserId).stream()
            .map(this::toDto)
            .toList();
    }

    private String generateNumber(DeclarationType dt, String contractorSymbol) {
        // Format: OSW/TYP_OPLATY/SKROT_KONTRAHENTA/ROK/MIESIAC/PODOKRES/WERSJA
        String feeType = dt.getCode().split("\\.")[0]; // e.g. "OP" from "OP.a"
        int year = LocalDateTime.now().getYear();
        int month = LocalDateTime.now().getMonthValue();
        int subperiod = 1;
        int version = 1;
        // Add random suffix to ensure uniqueness
        int rand = ThreadLocalRandom.current().nextInt(100, 999);
        return String.format("OSW/%s/%s/%d/%02d/%02d/%02d/%03d",
            feeType, contractorSymbol, year, month, subperiod, version, rand);
    }

    private DeclarationDto toDto(Declaration d) {
        String statusLabel = switch (d.getStatus()) {
            case NIE_ZLOZONE -> "Nie złożone";
            case ROBOCZE -> "Robocze";
            case ZLOZONE -> "Złożone";
        };
        return new DeclarationDto(
            d.getId(),
            d.getDeclarationNumber(),
            d.getStatus().name(),
            statusLabel,
            d.getDeclarationType().getCode(),
            d.getDeclarationType().getName(),
            d.getCreatedAt()
        );
    }
}
