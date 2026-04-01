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

    def keycloakAdminService = Mock(KeycloakAdminService)
    def assignmentRepository = Mock(UserContractorTypeAssignmentRepository)
    def contractorTypeRepository = Mock(ContractorTypeRepository)
    def contractorDataRepository = Mock(ContractorDataRepository)

    @Subject
    def service = new UserAssignmentService(
        keycloakAdminService,
        assignmentRepository,
        contractorTypeRepository,
        contractorDataRepository
    )

    def user1 = new KeycloakUserDto("id-1", "jan@pku.pl", "Jan", "Kowalski", "jan@pku.pl")
    def user2 = new KeycloakUserDto("id-2", "anna@pku.pl", "Anna", "Nowak", "anna@pku.pl")

    // --- listKontrahentUsersWithTypes ---

    def "listKontrahentUsersWithTypes returns users with their assignments and contractor data"() {
        given:
        def ct = new ContractorType(1L, "OSDp", "Operator", true)
        def assignment = new UserContractorTypeAssignment(1L, "id-1", ct)

        def cd = new ContractorData()
        cd.keycloakUserId = "id-1"
        cd.contractorAbbreviation = "ABC"
        cd.contractorFullName = "ABC S.A."
        cd.contractorShortName = "ABC"

        keycloakAdminService.getKontrahentUsers() >> [user1, user2]
        assignmentRepository.findByKeycloakUserIdIn(["id-1", "id-2"]) >> [assignment]
        contractorDataRepository.findByKeycloakUserIdIn(["id-1", "id-2"]) >> [cd]

        when:
        def result = service.listKontrahentUsersWithTypes()

        then:
        result.size() == 2

        result[0].keycloakUserId == "id-1"
        result[0].assignedType != null
        result[0].assignedType.symbol == "OSDp"
        result[0].contractorData != null
        result[0].contractorData.contractorAbbreviation() == "ABC"

        result[1].keycloakUserId == "id-2"
        result[1].assignedType == null
        result[1].contractorData == null
    }

    // --- updateAssignment ---

    def "updateAssignment creates new assignment when none exists"() {
        given:
        def ct = new ContractorType(1L, "OSDp", "Operator", true)
        contractorTypeRepository.findById(1L) >> Optional.of(ct)
        assignmentRepository.findByKeycloakUserId("id-1") >> Optional.empty()
        contractorDataRepository.findByKeycloakUserId("id-1") >> Optional.empty()
        keycloakAdminService.getKontrahentUsers() >> [user1]

        when:
        def result = service.updateAssignment("id-1", 1L)

        then:
        1 * assignmentRepository.save({ UserContractorTypeAssignment a ->
            a.keycloakUserId == "id-1" && a.contractorType.id == 1L
        })
        result.assignedType.symbol == "OSDp"
    }

    def "updateAssignment updates existing assignment"() {
        given:
        def oldCt = new ContractorType(1L, "OSDp", "Operator", true)
        def newCt = new ContractorType(2L, "OK", "Odbiorca", true)
        def existing = new UserContractorTypeAssignment(10L, "id-1", oldCt)

        contractorTypeRepository.findById(2L) >> Optional.of(newCt)
        assignmentRepository.findByKeycloakUserId("id-1") >> Optional.of(existing)
        contractorDataRepository.findByKeycloakUserId("id-1") >> Optional.empty()
        keycloakAdminService.getKontrahentUsers() >> [user1]

        when:
        def result = service.updateAssignment("id-1", 2L)

        then:
        1 * assignmentRepository.save({ UserContractorTypeAssignment a ->
            a.contractorType.id == 2L
        })
        result.assignedType.symbol == "OK"
    }

    def "updateAssignment with null typeId removes assignment"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> [user1]
        contractorDataRepository.findByKeycloakUserId("id-1") >> Optional.empty()

        when:
        def result = service.updateAssignment("id-1", null)

        then:
        1 * assignmentRepository.deleteByKeycloakUserId("id-1")
        1 * assignmentRepository.flush()
        result.assignedType == null
    }

    def "updateAssignment throws when contractor type not found"() {
        given:
        contractorTypeRepository.findById(99L) >> Optional.empty()

        when:
        service.updateAssignment("id-1", 99L)

        then:
        thrown(ResourceNotFoundException)
    }

    // --- updateAgreementNumber ---

    def "updateAgreementNumber updates existing contractor data"() {
        given:
        def cd = new ContractorData()
        cd.keycloakUserId = "id-1"
        cd.contractorAbbreviation = "ABC"
        cd.contractorFullName = "ABC S.A."
        cd.contractorShortName = "ABC"

        contractorDataRepository.findByKeycloakUserId("id-1") >> Optional.of(cd)

        when:
        service.updateAgreementNumber("id-1", "UPE/OSDp/ABC/2024")

        then:
        1 * contractorDataRepository.save({ ContractorData d ->
            d.agreementNumber == "UPE/OSDp/ABC/2024"
        })
    }

    def "updateAgreementNumber creates new contractor data when none exists"() {
        given:
        contractorDataRepository.findByKeycloakUserId("id-1") >> Optional.empty()

        when:
        service.updateAgreementNumber("id-1", "UPE/NEW/2024")

        then:
        1 * contractorDataRepository.save({ ContractorData d ->
            d.keycloakUserId == "id-1" &&
            d.agreementNumber == "UPE/NEW/2024"
        })
    }

    // --- getCurrentUserContractorData ---

    def "getCurrentUserContractorData returns full data for user"() {
        given:
        def ct = new ContractorType(1L, "OSDp", "Operator", true)
        def assignment = new UserContractorTypeAssignment(1L, "id-1", ct)

        def cd = new ContractorData()
        cd.keycloakUserId = "id-1"
        cd.contractorAbbreviation = "ABC"
        cd.contractorFullName = "ABC S.A."
        cd.contractorShortName = "ABC"
        cd.agreementNumber = "UPE/OSDp/ABC/2024"

        keycloakAdminService.getKontrahentUsers() >> [user1]
        contractorDataRepository.findByKeycloakUserId("id-1") >> Optional.of(cd)
        assignmentRepository.findByKeycloakUserId("id-1") >> Optional.of(assignment)

        when:
        def result = service.getCurrentUserContractorData("id-1")

        then:
        result.firstName() == "Jan"
        result.lastName() == "Kowalski"
        result.agreementNumber() == "UPE/OSDp/ABC/2024"
        result.contractorFullName() == "ABC S.A."
        result.assignedType().symbol() == "OSDp"
    }

    def "getCurrentUserContractorData returns nulls when user has no data"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> [user1]
        contractorDataRepository.findByKeycloakUserId("id-1") >> Optional.empty()
        assignmentRepository.findByKeycloakUserId("id-1") >> Optional.empty()

        when:
        def result = service.getCurrentUserContractorData("id-1")

        then:
        result.firstName() == "Jan"
        result.agreementNumber() == null
        result.contractorFullName() == null
        result.assignedType() == null
    }

    def "getCurrentUserContractorData throws when user not found in Keycloak"() {
        given:
        keycloakAdminService.getKontrahentUsers() >> []

        when:
        service.getCurrentUserContractorData("unknown")

        then:
        thrown(ResourceNotFoundException)
    }
}
