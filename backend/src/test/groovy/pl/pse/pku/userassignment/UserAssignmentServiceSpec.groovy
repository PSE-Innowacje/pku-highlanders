package pl.pse.pku.userassignment

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

    @Subject
    UserAssignmentService service = new UserAssignmentService(
        keycloakAdminService, assignmentRepository, contractorTypeRepository)

    private static final String USER_ID = "user-uuid-1"

    private KeycloakUserDto userDto(String id = USER_ID) {
        new KeycloakUserDto(id, "jkowalski", "Jan", "Kowalski", "jan@example.com")
    }

    private ContractorType contractorType(Long id = 10L) {
        new ContractorType(id, "OSDp", "Operator", false)
    }

    // --- listKontrahentUsersWithTypes ---

    def "listKontrahentUsersWithTypes returns users with their assigned contractor types"() {
        given:
        def user = userDto()
        def type = contractorType()
        def assignment = new UserContractorTypeAssignment(1L, USER_ID, type)

        keycloakAdminService.getKontrahentUsers() >> [user]
        assignmentRepository.findByKeycloakUserIdIn([USER_ID]) >> [assignment]

        when:
        def result = service.listKontrahentUsersWithTypes()

        then:
        result.size() == 1
        result[0].keycloakUserId() == USER_ID
        result[0].username() == "jkowalski"
        result[0].assignedType() != null
        result[0].assignedType().symbol() == "OSDp"
    }

    def "listKontrahentUsersWithTypes returns null assignedType for users without assignment"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> [userDto()]
        assignmentRepository.findByKeycloakUserIdIn([USER_ID]) >> []

        when:
        def result = service.listKontrahentUsersWithTypes()

        then:
        result[0].assignedType() == null
    }

    // --- updateAssignment ---

    def "updateAssignment creates new assignment when user has none"() {
        given:
        def type = contractorType()
        contractorTypeRepository.findById(10L) >> Optional.of(type)
        assignmentRepository.findByKeycloakUserId(USER_ID) >> Optional.empty()
        assignmentRepository.save(_) >> { UserContractorTypeAssignment a -> a }
        keycloakAdminService.getKontrahentUsers() >> [userDto()]

        when:
        def result = service.updateAssignment(USER_ID, 10L)

        then:
        1 * assignmentRepository.save({ UserContractorTypeAssignment a ->
            a.keycloakUserId == USER_ID && a.contractorType == type
        })
        result.assignedType().symbol() == "OSDp"
    }

    def "updateAssignment updates existing assignment when user already has one"() {
        given:
        def newType = contractorType(20L)
        def existingAssignment = new UserContractorTypeAssignment(1L, USER_ID, contractorType())
        contractorTypeRepository.findById(20L) >> Optional.of(newType)
        assignmentRepository.findByKeycloakUserId(USER_ID) >> Optional.of(existingAssignment)
        assignmentRepository.save(existingAssignment) >> existingAssignment
        keycloakAdminService.getKontrahentUsers() >> [userDto()]

        when:
        def result = service.updateAssignment(USER_ID, 20L)

        then:
        existingAssignment.contractorType == newType
        1 * assignmentRepository.save(existingAssignment)
        result.assignedType() != null
    }

    def "updateAssignment removes assignment when contractorTypeId is null"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> [userDto()]

        when:
        def result = service.updateAssignment(USER_ID, null)

        then:
        1 * assignmentRepository.deleteByKeycloakUserId(USER_ID)
        1 * assignmentRepository.flush()
        result.assignedType() == null
    }

    def "updateAssignment throws ResourceNotFoundException when contractor type does not exist"() {
        given:
        contractorTypeRepository.findById(99L) >> Optional.empty()

        when:
        service.updateAssignment(USER_ID, 99L)

        then:
        thrown(ResourceNotFoundException)
    }

    def "updateAssignment throws ResourceNotFoundException when user is not found in Keycloak"() {
        given:
        def type = contractorType()
        contractorTypeRepository.findById(10L) >> Optional.of(type)
        assignmentRepository.findByKeycloakUserId(USER_ID) >> Optional.empty()
        assignmentRepository.save(_) >> { UserContractorTypeAssignment a -> a }
        keycloakAdminService.getKontrahentUsers() >> [] // user not in Keycloak

        when:
        service.updateAssignment(USER_ID, 10L)

        then:
        thrown(ResourceNotFoundException)
    }
}
