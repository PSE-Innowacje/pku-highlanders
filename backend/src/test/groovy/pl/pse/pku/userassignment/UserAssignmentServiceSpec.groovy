package pl.pse.pku.userassignment

import pl.pse.pku.contractordata.ContractorData
import pl.pse.pku.contractordata.ContractorDataRepository
import pl.pse.pku.contractortype.ContractorType
import pl.pse.pku.contractortype.ContractorTypeRepository
import pl.pse.pku.exception.ResourceNotFoundException
import pl.pse.pku.keycloak.KeycloakAdminService
import pl.pse.pku.keycloak.KeycloakUserDto
import spock.lang.Specification
import spock.lang.Subject

class UserAssignmentServiceSpec extends Specification {

    KeycloakAdminService keycloakAdminService = Mock()
    UserContractorTypeAssignmentRepository assignmentRepository = Mock()
    ContractorTypeRepository contractorTypeRepository = Mock()
    ContractorDataRepository contractorDataRepository = Mock()

    @Subject
    UserAssignmentService service = new UserAssignmentService(
        keycloakAdminService, assignmentRepository, contractorTypeRepository, contractorDataRepository)

    private static final String USER_ID = "user-uuid-1"

    private KeycloakUserDto userDto(String id = USER_ID) {
        new KeycloakUserDto(id, "jkowalski", "Jan", "Kowalski", "jan@example.com")
    }

    private ContractorType contractorType(Long id = 10L) {
        new ContractorType(id, "OSDp", "Operator", false)
    }

    private ContractorData contractorData(String userId = USER_ID) {
        new ContractorData(1L, userId, "OSDp", "Operator Systemu", "Op. Sys.", "12345", "1234567890",
            "ul. Przykładowa 1", "CODE01", "AGR/2025/001", null, null)
    }

    // --- listKontrahentUsersWithTypes ---

    def "listKontrahentUsersWithTypes returns users with their assigned contractor types"() {
        given:
        def user = userDto()
        def type = contractorType()
        def assignment = new UserContractorTypeAssignment(1L, USER_ID, type)

        keycloakAdminService.getKontrahentUsers() >> [user]
        assignmentRepository.findByKeycloakUserIdIn([USER_ID]) >> [assignment]
        contractorDataRepository.findByKeycloakUserIdIn([USER_ID]) >> []

        when:
        def result = service.listKontrahentUsersWithTypes()

        then:
        result.size() == 1
        with(result[0]) {
            keycloakUserId() == USER_ID
            username() == "jkowalski"
            assignedTypes().size() == 1
            assignedTypes()[0].symbol() == "OSDp"
            contractorData() == null
        }
    }

    def "listKontrahentUsersWithTypes includes contractorData when available"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> [userDto()]
        assignmentRepository.findByKeycloakUserIdIn([USER_ID]) >> []
        contractorDataRepository.findByKeycloakUserIdIn([USER_ID]) >> [contractorData()]

        when:
        def result = service.listKontrahentUsersWithTypes()

        then:
        result[0].assignedTypes().isEmpty()
        result[0].contractorData() != null
        result[0].contractorData().agreementNumber() == "AGR/2025/001"
    }

    def "listKontrahentUsersWithTypes returns empty assignedTypes for users without assignments"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> [userDto()]
        assignmentRepository.findByKeycloakUserIdIn([USER_ID]) >> []
        contractorDataRepository.findByKeycloakUserIdIn([USER_ID]) >> []

        when:
        def result = service.listKontrahentUsersWithTypes()

        then:
        result[0].assignedTypes().isEmpty()
        result[0].contractorData() == null
    }

    def "listKontrahentUsersWithTypes returns empty list when no Keycloak users exist"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> []
        assignmentRepository.findByKeycloakUserIdIn([]) >> []
        contractorDataRepository.findByKeycloakUserIdIn([]) >> []

        when:
        def result = service.listKontrahentUsersWithTypes()

        then:
        result.isEmpty()
    }

    // --- updateAssignments ---

    def "updateAssignments replaces all assignments with new contractor types"() {
        given:
        def type = contractorType()
        contractorTypeRepository.findAllById([10L]) >> [type]
        contractorDataRepository.findByKeycloakUserId(USER_ID) >> Optional.empty()
        keycloakAdminService.getKontrahentUsers() >> [userDto()]

        when:
        def result = service.updateAssignments(USER_ID, [10L])

        then:
        1 * assignmentRepository.deleteAllByKeycloakUserId(USER_ID)
        1 * assignmentRepository.flush()
        1 * assignmentRepository.save({ UserContractorTypeAssignment a ->
            a.keycloakUserId == USER_ID && a.contractorType == type
        })
        result.assignedTypes().size() == 1
        result.assignedTypes()[0].symbol() == "OSDp"
    }

    def "updateAssignments clears all assignments when list is empty"() {
        given:
        contractorDataRepository.findByKeycloakUserId(USER_ID) >> Optional.empty()
        keycloakAdminService.getKontrahentUsers() >> [userDto()]

        when:
        def result = service.updateAssignments(USER_ID, [])

        then:
        1 * assignmentRepository.deleteAllByKeycloakUserId(USER_ID)
        1 * assignmentRepository.flush()
        0 * assignmentRepository.save(_)
        result.assignedTypes().isEmpty()
    }

    def "updateAssignments clears all assignments when list is null"() {
        given:
        contractorDataRepository.findByKeycloakUserId(USER_ID) >> Optional.empty()
        keycloakAdminService.getKontrahentUsers() >> [userDto()]

        when:
        def result = service.updateAssignments(USER_ID, null)

        then:
        1 * assignmentRepository.deleteAllByKeycloakUserId(USER_ID)
        0 * assignmentRepository.save(_)
        result.assignedTypes().isEmpty()
    }

    def "updateAssignments includes contractorData in returned DTO when it exists"() {
        given:
        contractorTypeRepository.findAllById([10L]) >> [contractorType()]
        contractorDataRepository.findByKeycloakUserId(USER_ID) >> Optional.of(contractorData())
        keycloakAdminService.getKontrahentUsers() >> [userDto()]

        when:
        def result = service.updateAssignments(USER_ID, [10L])

        then:
        result.contractorData() != null
        result.contractorData().agreementNumber() == "AGR/2025/001"
    }

    def "updateAssignments throws ResourceNotFoundException when user not found in Keycloak"() {
        given:
        contractorDataRepository.findByKeycloakUserId(USER_ID) >> Optional.empty()
        keycloakAdminService.getKontrahentUsers() >> []

        when:
        service.updateAssignments(USER_ID, [])

        then:
        thrown(ResourceNotFoundException)
    }

    // --- updateAgreementNumber ---

    def "updateAgreementNumber updates agreement number on existing ContractorData"() {
        given:
        def data = contractorData()
        contractorDataRepository.findByKeycloakUserId(USER_ID) >> Optional.of(data)

        when:
        service.updateAgreementNumber(USER_ID, "NEW/AGR/2026")

        then:
        1 * contractorDataRepository.save({ ContractorData d ->
            d.agreementNumber == "NEW/AGR/2026"
        })
    }

    def "updateAgreementNumber creates new ContractorData when none exists"() {
        given:
        contractorDataRepository.findByKeycloakUserId(USER_ID) >> Optional.empty()

        when:
        service.updateAgreementNumber(USER_ID, "NEW/AGR/2026")

        then:
        1 * contractorDataRepository.save({ ContractorData d ->
            d.keycloakUserId == USER_ID &&
            d.agreementNumber == "NEW/AGR/2026" &&
            d.contractorAbbreviation == "" &&
            d.contractorFullName == "" &&
            d.contractorShortName == ""
        })
    }

    // --- getCurrentUserContractorData ---

    def "getCurrentUserContractorData returns full DTO with assignments and contractor data"() {
        given:
        def type = contractorType()
        def assignment = new UserContractorTypeAssignment(1L, USER_ID, type)
        keycloakAdminService.getKontrahentUsers() >> [userDto()]
        contractorDataRepository.findByKeycloakUserId(USER_ID) >> Optional.of(contractorData())
        assignmentRepository.findAllByKeycloakUserId(USER_ID) >> [assignment]

        when:
        def result = service.getCurrentUserContractorData(USER_ID)

        then:
        result.firstName() == "Jan"
        result.lastName() == "Kowalski"
        result.agreementNumber() == "AGR/2025/001"
        result.contractorFullName() == "Operator Systemu"
        result.contractorAbbreviation() == "OSDp"
        result.assignedTypes().size() == 1
        result.assignedTypes()[0].symbol() == "OSDp"
        result.contractorData() != null
    }

    def "getCurrentUserContractorData returns null fields when no contractor data exists"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> [userDto()]
        contractorDataRepository.findByKeycloakUserId(USER_ID) >> Optional.empty()
        assignmentRepository.findAllByKeycloakUserId(USER_ID) >> []

        when:
        def result = service.getCurrentUserContractorData(USER_ID)

        then:
        result.firstName() == "Jan"
        result.agreementNumber() == null
        result.contractorFullName() == null
        result.contractorAbbreviation() == null
        result.assignedTypes().isEmpty()
        result.contractorData() == null
    }

    def "getCurrentUserContractorData throws ResourceNotFoundException when user not in Keycloak"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> []

        when:
        service.getCurrentUserContractorData(USER_ID)

        then:
        thrown(ResourceNotFoundException)
    }
}
