package pl.pse.pku.declaration

import pl.pse.pku.contractordata.ContractorData
import pl.pse.pku.contractordata.ContractorDataRepository
import pl.pse.pku.contractortype.ContractorType
import pl.pse.pku.contractortype.ContractorTypeRepository
import pl.pse.pku.declarationtype.DeclarationType
import pl.pse.pku.declarationtype.DeclarationTypeField
import pl.pse.pku.declarationtype.DeclarationTypeRepository
import pl.pse.pku.exception.BusinessException
import pl.pse.pku.exception.ResourceNotFoundException
import pl.pse.pku.keycloak.KeycloakAdminService
import pl.pse.pku.keycloak.KeycloakUserDto
import pl.pse.pku.userassignment.UserContractorTypeAssignment
import pl.pse.pku.userassignment.UserContractorTypeAssignmentRepository
import spock.lang.Specification
import spock.lang.Subject

class DeclarationServiceSpec extends Specification {

    def declarationRepository = Mock(DeclarationRepository)
    def assignmentRepository = Mock(UserContractorTypeAssignmentRepository)
    def contractorDataRepository = Mock(ContractorDataRepository)
    def contractorTypeRepository = Mock(ContractorTypeRepository)
    def declarationTypeRepository = Mock(DeclarationTypeRepository)
    def keycloakAdminService = Mock(KeycloakAdminService)

    @Subject
    def service = new DeclarationService(
        declarationRepository,
        assignmentRepository,
        contractorDataRepository,
        contractorTypeRepository,
        declarationTypeRepository,
        keycloakAdminService
    )

    // --- getMyDeclarations ---

    def "getMyDeclarations returns declarations for given user"() {
        given:
        def userId = "user-1"
        def decl = makeDeclaration(1L, "OSW/OP/ABC/2026/04/01/01/123", DeclarationStatus.NIE_ZLOZONE, userId)

        when:
        def result = service.getMyDeclarations(userId)

        then:
        1 * declarationRepository.findByKeycloakUserIdOrderByCreatedAtDesc(userId) >> [decl]
        result.size() == 1
        result[0].declarationNumber == decl.declarationNumber
        result[0].statusLabel == "Nie złożone"
    }

    def "getMyDeclarations returns empty list when no declarations"() {
        when:
        def result = service.getMyDeclarations("no-user")

        then:
        1 * declarationRepository.findByKeycloakUserIdOrderByCreatedAtDesc("no-user") >> []
        result.isEmpty()
    }

    // --- getDeclarationDetail ---

    def "getDeclarationDetail returns detail for owned declaration"() {
        given:
        def decl = makeDeclaration(1L, "OSW/OP/ABC/2026/04/01/01/123", DeclarationStatus.ROBOCZE, "user-1")

        when:
        def result = service.getDeclarationDetail(1L, "user-1")

        then:
        1 * declarationRepository.findById(1L) >> Optional.of(decl)
        result.id == 1L
        result.status == "ROBOCZE"
    }

    def "getDeclarationDetail throws when declaration not found"() {
        given:
        declarationRepository.findById(99L) >> Optional.empty()

        when:
        service.getDeclarationDetail(99L, "user-1")

        then:
        thrown(ResourceNotFoundException)
    }

    def "getDeclarationDetail throws when user does not own declaration"() {
        given:
        def decl = makeDeclaration(1L, "OSW/OP/ABC/2026/04/01/01/123", DeclarationStatus.NIE_ZLOZONE, "other-user")
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.getDeclarationDetail(1L, "user-1")

        then:
        thrown(BusinessException)
    }

    // --- saveDeclaration ---

    def "saveDeclaration saves field values and sets status to ROBOCZE"() {
        given:
        def decl = makeDeclaration(1L, "OSW/OP/ABC/2026/04/01/01/123", DeclarationStatus.NIE_ZLOZONE, "user-1")
        def fieldValues = ["FIELD1": "100"]

        when:
        service.saveDeclaration(1L, "user-1", fieldValues, "komentarz")

        then:
        1 * declarationRepository.findById(1L) >> Optional.of(decl)
        1 * declarationRepository.save({ Declaration d ->
            d.status == DeclarationStatus.ROBOCZE &&
            d.fieldValues == fieldValues &&
            d.comment == "komentarz"
        }) >> decl
    }

    def "saveDeclaration throws when declaration is already submitted"() {
        given:
        def decl = makeDeclaration(1L, "OSW/OP/ABC/2026/04/01/01/123", DeclarationStatus.ZLOZONE, "user-1")
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.saveDeclaration(1L, "user-1", [:], null)

        then:
        thrown(BusinessException)
    }

    def "saveDeclaration validates field value exceeding integer digits"() {
        given:
        def field = new DeclarationTypeField()
        field.position = "1"
        field.fieldCode = "F1"
        field.dataType = "Number (3,2)"
        field.fieldName = "Pole testowe"
        field.required = false
        field.unit = "kW"

        def declType = new DeclarationType()
        declType.code = "OP.OSDp"
        declType.name = "Test"
        declType.contractorTypes = "OSDp"
        declType.hasComment = false
        declType.fields = [field]

        def decl = new Declaration()
        decl.id = 1L
        decl.declarationNumber = "OSW/OP/ABC/2026/04/01/01/123"
        decl.status = DeclarationStatus.NIE_ZLOZONE
        decl.keycloakUserId = "user-1"
        decl.declarationType = declType
        decl.createdAt = java.time.LocalDateTime.now()
        decl.fieldValues = [:]

        field.declarationType = declType
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.saveDeclaration(1L, "user-1", ["F1": "12345.12"], null)

        then:
        def e = thrown(BusinessException)
        e.message.contains("część całkowita")
    }

    def "saveDeclaration validates field value exceeding decimal digits"() {
        given:
        def field = new DeclarationTypeField()
        field.position = "1"
        field.fieldCode = "F1"
        field.dataType = "Number (9,2)"
        field.fieldName = "Pole testowe"
        field.required = false
        field.unit = "kW"

        def declType = new DeclarationType()
        declType.code = "OP.OSDp"
        declType.name = "Test"
        declType.contractorTypes = "OSDp"
        declType.hasComment = false
        declType.fields = [field]

        def decl = new Declaration()
        decl.id = 1L
        decl.declarationNumber = "OSW/OP/ABC/2026/04/01/01/123"
        decl.status = DeclarationStatus.ROBOCZE
        decl.keycloakUserId = "user-1"
        decl.declarationType = declType
        decl.createdAt = java.time.LocalDateTime.now()
        decl.fieldValues = [:]

        field.declarationType = declType
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.saveDeclaration(1L, "user-1", ["F1": "123.12345"], null)

        then:
        def e = thrown(BusinessException)
        e.message.contains("część dziesiętna")
    }

    // --- submitDeclaration ---

    def "submitDeclaration throws when status is not ROBOCZE"() {
        given:
        def decl = makeDeclaration(1L, "OSW/OP/ABC/2026/04/01/01/123", DeclarationStatus.NIE_ZLOZONE, "user-1")
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.submitDeclaration(1L, "user-1")

        then:
        thrown(BusinessException)
    }

    def "submitDeclaration throws when required field is missing"() {
        given:
        def field = new DeclarationTypeField()
        field.position = "1"
        field.fieldCode = "REQ1"
        field.dataType = "Number"
        field.fieldName = "Wymagane pole"
        field.required = true
        field.unit = "szt"

        def declType = new DeclarationType()
        declType.code = "OP.OSDp"
        declType.name = "Test"
        declType.contractorTypes = "OSDp"
        declType.hasComment = false
        declType.fields = [field]

        def decl = new Declaration()
        decl.id = 1L
        decl.declarationNumber = "OSW/OP/ABC/2026/04/01/01/123"
        decl.status = DeclarationStatus.ROBOCZE
        decl.keycloakUserId = "user-1"
        decl.declarationType = declType
        decl.createdAt = java.time.LocalDateTime.now()
        decl.fieldValues = [:]

        field.declarationType = declType
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.submitDeclaration(1L, "user-1")

        then:
        def e = thrown(BusinessException)
        e.message.contains("REQ1")
    }

    def "submitDeclaration returns JSON with contractor data"() {
        given:
        def field = new DeclarationTypeField()
        field.position = "1"
        field.fieldCode = "F1"
        field.dataType = "Number (9,3)"
        field.fieldName = "Pole"
        field.required = true
        field.unit = "kW"

        def declType = new DeclarationType()
        declType.code = "OP.OSDp"
        declType.name = "Opłata przejściowa"
        declType.contractorTypes = "OSDp"
        declType.hasComment = false
        declType.fields = [field]

        def decl = new Declaration()
        decl.id = 1L
        decl.declarationNumber = "OSW/OP/ABC/2026/04/01/01/123"
        decl.status = DeclarationStatus.ROBOCZE
        decl.keycloakUserId = "user-1"
        decl.declarationType = declType
        decl.createdAt = java.time.LocalDateTime.now()
        decl.fieldValues = ["F1": "123.456"]

        field.declarationType = declType

        def ct = new ContractorType(1L, "OSDp", "Operator", true)
        def assignment = new UserContractorTypeAssignment(1L, "user-1", ct)

        def cd = new ContractorData()
        cd.keycloakUserId = "user-1"
        cd.contractorAbbreviation = "ABC"
        cd.contractorFullName = "ABC Sp. z o.o."
        cd.contractorShortName = "ABC"
        cd.nip = "1234567890"
        cd.krs = "0000012345"
        cd.agreementNumber = "UPE/OSDp/ABC/2024"

        declarationRepository.findById(1L) >> Optional.of(decl)
        declarationRepository.save(_) >> decl
        assignmentRepository.findByKeycloakUserId("user-1") >> Optional.of(assignment)
        contractorDataRepository.findByKeycloakUserId("user-1") >> Optional.of(cd)
        keycloakAdminService.getKontrahentUsers() >> [new KeycloakUserDto("user-1", "abc@pku.pl", "Jan", "Kowalski", "abc@pku.pl")]

        when:
        def json = service.submitDeclaration(1L, "user-1")

        then:
        json.declarationNumber == "OSW/OP/ABC/2026/04/01/01/123"
        json.status == "Złożone"
        json.contractor.contractorAbbreviation == "ABC"
        json.contractor.contractorFullName == "ABC Sp. z o.o."
        json.contractor.nip == "1234567890"
        json.contractor.agreementNumber == "UPE/OSDp/ABC/2024"
        json.submitter.firstName == "Jan"
        json.submitter.lastName == "Kowalski"
        json.fields.F1.value == "123.456"
    }

    // --- generateDeclarationsForSchedule ---

    def "generateDeclarationsForSchedule creates declarations for all users with matching contractor type"() {
        given:
        def declType = new DeclarationType()
        declType.id = 10L
        declType.code = "OP.OSDp"
        declType.name = "Test"
        declType.contractorTypes = "OSDp"
        declType.hasComment = false
        declType.fields = []

        def ct = new ContractorType(1L, "OSDp", "Operator", true)
        def assignment1 = new UserContractorTypeAssignment(1L, "user-1", ct)
        def assignment2 = new UserContractorTypeAssignment(2L, "user-2", ct)

        declarationTypeRepository.findByCode("OP.OSDp") >> Optional.of(declType)
        contractorTypeRepository.findByDeclarationTypeId(10L) >> [ct]
        assignmentRepository.findByContractorTypeId(1L) >> [assignment1, assignment2]
        declarationRepository.existsByKeycloakUserIdAndDeclarationTypeIdAndScheduleDay(_, _, _) >> false

        when:
        def count = service.generateDeclarationsForSchedule("OP.OSDp", 15)

        then:
        count == 2
        2 * declarationRepository.save({ Declaration d ->
            d.scheduleDay == 15 &&
            d.status == DeclarationStatus.NIE_ZLOZONE &&
            d.declarationNumber.contains("OSDp")
        })
    }

    def "generateDeclarationsForSchedule skips already existing declarations"() {
        given:
        def declType = new DeclarationType()
        declType.id = 10L
        declType.code = "OP.OSDp"
        declType.name = "Test"
        declType.contractorTypes = "OSDp"
        declType.hasComment = false
        declType.fields = []

        def ct = new ContractorType(1L, "OSDp", "Operator", true)
        def assignment = new UserContractorTypeAssignment(1L, "user-1", ct)

        declarationTypeRepository.findByCode("OP.OSDp") >> Optional.of(declType)
        contractorTypeRepository.findByDeclarationTypeId(10L) >> [ct]
        assignmentRepository.findByContractorTypeId(1L) >> [assignment]
        declarationRepository.existsByKeycloakUserIdAndDeclarationTypeIdAndScheduleDay("user-1", 10L, 1) >> true

        when:
        def count = service.generateDeclarationsForSchedule("OP.OSDp", 1)

        then:
        count == 0
        0 * declarationRepository.save(_)
    }

    def "generateDeclarationsForSchedule returns 0 when no contractor types linked"() {
        given:
        def declType = new DeclarationType()
        declType.id = 10L
        declType.code = "OP.OSDp"

        declarationTypeRepository.findByCode("OP.OSDp") >> Optional.of(declType)
        contractorTypeRepository.findByDeclarationTypeId(10L) >> []

        when:
        def count = service.generateDeclarationsForSchedule("OP.OSDp", 1)

        then:
        count == 0
    }

    def "generateDeclarationsForSchedule throws for unknown declaration type"() {
        given:
        declarationTypeRepository.findByCode("UNKNOWN") >> Optional.empty()

        when:
        service.generateDeclarationsForSchedule("UNKNOWN", 1)

        then:
        thrown(ResourceNotFoundException)
    }

    // --- helpers ---

    private Declaration makeDeclaration(Long id, String number, DeclarationStatus status, String userId) {
        def declType = new DeclarationType()
        declType.code = "OP.OSDp"
        declType.name = "Opłata przejściowa"
        declType.contractorTypes = "OSDp"
        declType.hasComment = false
        declType.fields = []

        def decl = new Declaration()
        decl.id = id
        decl.declarationNumber = number
        decl.status = status
        decl.keycloakUserId = userId
        decl.declarationType = declType
        decl.createdAt = java.time.LocalDateTime.now()
        decl.fieldValues = [:]
        return decl
    }
}
