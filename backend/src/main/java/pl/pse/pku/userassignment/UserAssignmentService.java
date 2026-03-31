package pl.pse.pku.userassignment;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pse.pku.contractortype.ContractorType;
import pl.pse.pku.contractortype.ContractorTypeDto;
import pl.pse.pku.contractortype.ContractorTypeRepository;
import pl.pse.pku.exception.ResourceNotFoundException;
import pl.pse.pku.keycloak.KeycloakAdminService;
import pl.pse.pku.keycloak.KeycloakUserDto;

@Service
@RequiredArgsConstructor
public class UserAssignmentService {

    private final KeycloakAdminService keycloakAdminService;
    private final UserContractorTypeAssignmentRepository assignmentRepository;
    private final ContractorTypeRepository contractorTypeRepository;

    @Transactional(readOnly = true)
    public List<KontrahentUserWithTypesDto> listKontrahentUsersWithTypes() {
        List<KeycloakUserDto> users = keycloakAdminService.getKontrahentUsers();
        List<String> userIds = users.stream().map(KeycloakUserDto::id).toList();

        Map<String, UserContractorTypeAssignment> assignmentsByUser =
            assignmentRepository.findByKeycloakUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserContractorTypeAssignment::getKeycloakUserId, Function.identity()));

        return users.stream()
            .map(user -> {
                var assignment = assignmentsByUser.get(user.id());
                ContractorTypeDto typeDto = assignment != null ? toDto(assignment.getContractorType()) : null;
                return new KontrahentUserWithTypesDto(
                    user.id(), user.username(), user.firstName(), user.lastName(), user.email(), typeDto);
            })
            .toList();
    }

    @Transactional
    public KontrahentUserWithTypesDto updateAssignment(String keycloakUserId, Long contractorTypeId) {
        ContractorTypeDto typeDto = null;

        if (contractorTypeId != null) {
            ContractorType type = contractorTypeRepository.findById(contractorTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono typu kontrahenta o id " + contractorTypeId));

            var existing = assignmentRepository.findByKeycloakUserId(keycloakUserId);
            if (existing.isPresent()) {
                existing.get().setContractorType(type);
                assignmentRepository.save(existing.get());
            } else {
                assignmentRepository.save(new UserContractorTypeAssignment(null, keycloakUserId, type));
            }
            typeDto = toDto(type);
        } else {
            assignmentRepository.deleteByKeycloakUserId(keycloakUserId);
            assignmentRepository.flush();
        }

        KeycloakUserDto user = keycloakAdminService.getKontrahentUsers().stream()
            .filter(u -> u.id().equals(keycloakUserId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono użytkownika w Keycloak"));

        return new KontrahentUserWithTypesDto(
            user.id(), user.username(), user.firstName(), user.lastName(), user.email(), typeDto);
    }

    private ContractorTypeDto toDto(ContractorType entity) {
        return new ContractorTypeDto(entity.getId(), entity.getSymbol(), entity.getName(), entity.isSystem());
    }
}
