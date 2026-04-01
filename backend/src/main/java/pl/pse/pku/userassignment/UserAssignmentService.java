package pl.pse.pku.userassignment;

import java.util.Collections;
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

        Map<String, List<UserContractorTypeAssignment>> assignmentsByUser =
            assignmentRepository.findByKeycloakUserIdIn(userIds).stream()
                .collect(Collectors.groupingBy(UserContractorTypeAssignment::getKeycloakUserId));

        Map<String, ContractorData> dataByUser =
            contractorDataRepository.findByKeycloakUserIdIn(userIds).stream()
                .collect(Collectors.toMap(ContractorData::getKeycloakUserId, Function.identity()));

        return users.stream()
            .map(user -> {
                var assignments = assignmentsByUser.getOrDefault(user.id(), Collections.emptyList());
                List<ContractorTypeDto> typeDtos = assignments.stream()
                    .map(a -> toDto(a.getContractorType()))
                    .toList();
                ContractorDataDto dataDto = toDataDto(dataByUser.get(user.id()));
                return new KontrahentUserWithTypesDto(
                    user.id(), user.username(), user.firstName(), user.lastName(), user.email(),
                    typeDtos, dataDto);
            })
            .toList();
    }

    @Transactional
    public KontrahentUserWithTypesDto updateAssignments(String keycloakUserId, List<Long> contractorTypeIds) {
        assignmentRepository.deleteAllByKeycloakUserId(keycloakUserId);
        assignmentRepository.flush();

        List<ContractorTypeDto> typeDtos = Collections.emptyList();
        if (contractorTypeIds != null && !contractorTypeIds.isEmpty()) {
            List<ContractorType> types = contractorTypeRepository.findAllById(contractorTypeIds);
            for (ContractorType type : types) {
                assignmentRepository.save(new UserContractorTypeAssignment(null, keycloakUserId, type));
            }
            typeDtos = types.stream().map(this::toDto).toList();
        }

        KeycloakUserDto user = findKeycloakUser(keycloakUserId);
        ContractorDataDto dataDto = toDataDto(contractorDataRepository.findByKeycloakUserId(keycloakUserId).orElse(null));

        return new KontrahentUserWithTypesDto(
            user.id(), user.username(), user.firstName(), user.lastName(), user.email(),
            typeDtos, dataDto);
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

        List<ContractorTypeDto> typeDtos = assignmentRepository.findAllByKeycloakUserId(keycloakUserId).stream()
            .map(a -> toDto(a.getContractorType()))
            .toList();

        return new CurrentUserContractorDto(
            user.firstName(),
            user.lastName(),
            data != null ? data.getAgreementNumber() : null,
            data != null ? data.getContractorFullName() : null,
            data != null ? data.getContractorAbbreviation() : null,
            typeDtos,
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
