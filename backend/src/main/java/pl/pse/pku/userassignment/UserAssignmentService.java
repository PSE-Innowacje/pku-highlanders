package pl.pse.pku.userassignment;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.pse.pku.contractordata.ContractorData;
import pl.pse.pku.contractordata.ContractorDataDto;
import pl.pse.pku.contractordata.ContractorDataRepository;
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
    private final ContractorDataRepository contractorDataRepository;

    @Transactional(readOnly = true)
    public List<KontrahentUserWithTypesDto> listKontrahentUsersWithTypes() {
        List<KeycloakUserDto> users = keycloakAdminService.getKontrahentUsers();
        List<String> userIds = users.stream().map(KeycloakUserDto::id).toList();

        Map<String, UserContractorTypeAssignment> assignmentsByUser =
            assignmentRepository.findByKeycloakUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserContractorTypeAssignment::getKeycloakUserId, Function.identity()));

        Map<String, ContractorData> dataByUser =
            contractorDataRepository.findByKeycloakUserIdIn(userIds).stream()
                .collect(Collectors.toMap(ContractorData::getKeycloakUserId, Function.identity()));

        return users.stream()
            .map(user -> {
                var assignment = assignmentsByUser.get(user.id());
                ContractorTypeDto typeDto = assignment != null ? toDto(assignment.getContractorType()) : null;
                ContractorDataDto dataDto = toDataDto(dataByUser.get(user.id()));
                return new KontrahentUserWithTypesDto(
                    user.id(), user.username(), user.firstName(), user.lastName(), user.email(),
                    typeDto, dataDto);
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

        KeycloakUserDto user = findKeycloakUser(keycloakUserId);
        ContractorDataDto dataDto = toDataDto(contractorDataRepository.findByKeycloakUserId(keycloakUserId).orElse(null));

        return new KontrahentUserWithTypesDto(
            user.id(), user.username(), user.firstName(), user.lastName(), user.email(),
            typeDto, dataDto);
    }

    @Transactional
    public void updateAgreementNumber(String keycloakUserId, String agreementNumber) {
        ContractorData data = contractorDataRepository.findByKeycloakUserId(keycloakUserId)
            .orElseGet(() -> {
                var newData = new ContractorData();
                newData.setKeycloakUserId(keycloakUserId);
                newData.setContractorAbbreviation("");
                newData.setContractorFullName("");
                newData.setContractorShortName("");
                return newData;
            });
        data.setAgreementNumber(agreementNumber);
        contractorDataRepository.save(data);
    }

    @Transactional(readOnly = true)
    public CurrentUserContractorDto getCurrentUserContractorData(String keycloakUserId) {
        KeycloakUserDto user = findKeycloakUser(keycloakUserId);
        ContractorData data = contractorDataRepository.findByKeycloakUserId(keycloakUserId).orElse(null);

        var assignment = assignmentRepository.findByKeycloakUserId(keycloakUserId).orElse(null);
        ContractorTypeDto typeDto = assignment != null ? toDto(assignment.getContractorType()) : null;

        return new CurrentUserContractorDto(
            user.firstName(),
            user.lastName(),
            data != null ? data.getAgreementNumber() : null,
            data != null ? data.getContractorFullName() : null,
            data != null ? data.getContractorAbbreviation() : null,
            typeDto,
            toDataDto(data));
    }

    private KeycloakUserDto findKeycloakUser(String keycloakUserId) {
        return keycloakAdminService.getKontrahentUsers().stream()
            .filter(u -> u.id().equals(keycloakUserId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono użytkownika w Keycloak"));
    }

    private ContractorTypeDto toDto(ContractorType entity) {
        return new ContractorTypeDto(entity.getId(), entity.getSymbol(), entity.getName(), entity.isSystem());
    }

    private ContractorDataDto toDataDto(ContractorData data) {
        if (data == null) return null;
        return new ContractorDataDto(
            data.getContractorAbbreviation(),
            data.getContractorFullName(),
            data.getContractorShortName(),
            data.getKrs(),
            data.getNip(),
            data.getRegisteredAddress(),
            data.getContractorCode(),
            data.getAgreementNumber(),
            data.getAgreementDateFrom(),
            data.getAgreementDateTo());
    }
}
