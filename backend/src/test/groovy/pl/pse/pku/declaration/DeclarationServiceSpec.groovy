package pl.pse.pku.declaration

import pl.pse.pku.declarationtype.DeclarationType
import pl.pse.pku.declarationtype.DeclarationTypeField
import pl.pse.pku.exception.BusinessException
import pl.pse.pku.exception.ResourceNotFoundException
import pl.pse.pku.contractortype.ContractorType
import pl.pse.pku.userassignment.UserContractorTypeAssignment
import pl.pse.pku.userassignment.UserContractorTypeAssignmentRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class DeclarationServiceSpec extends Specification {

    DeclarationRepository declarationRepository = Mock()
    UserContractorTypeAssignmentRepository assignmentRepository = Mock()

    @Subject
    DeclarationService service = new DeclarationService(declarationRepository, assignmentRepository)

    private static final String USER_ID = "user-uuid-123"

    // --- helpers ---

    private DeclarationType buildDeclarationType(boolean hasComment = false, List<DeclarationTypeField> fields = []) {
        def dt = new DeclarationType(1L, "OP.01", "Oświadczenie", "OSDp", hasComment, fields)
        fields.each { it.declarationType = dt }
        dt
    }

    private DeclarationTypeField buildField(String code, String name, boolean required) {
        def f = new DeclarationTypeField()
        f.position = "1"
        f.fieldCode = code
        f.dataType = "TEXT"
        f.fieldName = name
        f.required = required
        f.unit = "-"
        f
    }

    private Declaration buildDeclaration(Long id, DeclarationStatus status, DeclarationType dt) {
        def d = new Declaration()
        d.id = id
        d.declarationNumber = "OSW/OP/SYM/2025/01/01/01/001"
        d.status = status
        d.keycloakUserId = USER_ID
        d.declarationType = dt
        d.createdAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        d.fieldValues = [:]
        d
    }

    // --- getMyDeclarations ---

    def "getMyDeclarations returns declarations for given user"() {
        given:
        def dt = buildDeclarationType()
        def decl = buildDeclaration(1L, DeclarationStatus.NIE_ZLOZONE, dt)
        declarationRepository.findByKeycloakUserIdOrderByCreatedAtDesc(USER_ID) >> [decl]

        when:
        def result = service.getMyDeclarations(USER_ID)

        then:
        result.size() == 1
        result[0].id() == 1L
        result[0].status() == "NIE_ZLOZONE"
        result[0].declarationTypeCode() == "OP.01"
    }

    // --- getDeclarationDetail ---

    def "getDeclarationDetail returns detail DTO for own declaration"() {
        given:
        def dt = buildDeclarationType()
        def decl = buildDeclaration(1L, DeclarationStatus.ROBOCZE, dt)
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        def result = service.getDeclarationDetail(1L, USER_ID)

        then:
        result.id() == 1L
        result.status() == "ROBOCZE"
        result.statusLabel() == "Robocze"
    }

    def "getDeclarationDetail throws BusinessException when accessing another user's declaration"() {
        given:
        def dt = buildDeclarationType()
        def decl = buildDeclaration(1L, DeclarationStatus.ROBOCZE, dt)
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.getDeclarationDetail(1L, "other-user-id")

        then:
        thrown(BusinessException)
    }

    def "getDeclarationDetail throws ResourceNotFoundException when declaration does not exist"() {
        given:
        declarationRepository.findById(99L) >> Optional.empty()

        when:
        service.getDeclarationDetail(99L, USER_ID)

        then:
        thrown(ResourceNotFoundException)
    }

    // --- saveDeclaration ---

    def "saveDeclaration updates fields and sets status to ROBOCZE for NIE_ZLOZONE declaration"() {
        given:
        def dt = buildDeclarationType()
        def decl = buildDeclaration(1L, DeclarationStatus.NIE_ZLOZONE, dt)
        declarationRepository.findById(1L) >> Optional.of(decl)
        declarationRepository.save(decl) >> decl

        when:
        def result = service.saveDeclaration(1L, USER_ID, ["F1": "value1"], "my comment")

        then:
        decl.status == DeclarationStatus.ROBOCZE
        decl.fieldValues == ["F1": "value1"]
        decl.comment == "my comment"
    }

    def "saveDeclaration updates fields when declaration is already in ROBOCZE status"() {
        given:
        def dt = buildDeclarationType()
        def decl = buildDeclaration(1L, DeclarationStatus.ROBOCZE, dt)
        declarationRepository.findById(1L) >> Optional.of(decl)
        declarationRepository.save(decl) >> decl

        when:
        service.saveDeclaration(1L, USER_ID, ["F1": "updated"], null)

        then:
        decl.fieldValues == ["F1": "updated"]
        decl.comment == null
    }

    def "saveDeclaration throws BusinessException when declaration is already submitted"() {
        given:
        def dt = buildDeclarationType()
        def decl = buildDeclaration(1L, DeclarationStatus.ZLOZONE, dt)
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.saveDeclaration(1L, USER_ID, [:], null)

        then:
        def ex = thrown(BusinessException)
        ex.message.contains("Złożone")
    }

    // --- submitDeclaration ---

    def "submitDeclaration transitions to ZLOZONE and returns JSON when all required fields are filled"() {
        given:
        def requiredField = buildField("POD", "Punkt Poboru", true)
        def dt = buildDeclarationType(false, [requiredField])
        def decl = buildDeclaration(1L, DeclarationStatus.ROBOCZE, dt)
        decl.fieldValues = ["POD": "12345"]
        declarationRepository.findById(1L) >> Optional.of(decl)
        declarationRepository.save(decl) >> decl

        when:
        def result = service.submitDeclaration(1L, USER_ID)

        then:
        decl.status == DeclarationStatus.ZLOZONE
        result.containsKey("declarationNumber")
        result.containsKey("fields")
        (result["fields"] as Map).containsKey("POD")
    }

    def "submitDeclaration throws BusinessException when declaration is not in ROBOCZE status"() {
        given:
        def dt = buildDeclarationType()
        def decl = buildDeclaration(1L, DeclarationStatus.NIE_ZLOZONE, dt)
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.submitDeclaration(1L, USER_ID)

        then:
        def ex = thrown(BusinessException)
        ex.message.contains("Robocze")
    }

    def "submitDeclaration throws BusinessException when required field is blank"() {
        given:
        def requiredField = buildField("POD", "Punkt Poboru", true)
        def dt = buildDeclarationType(false, [requiredField])
        def decl = buildDeclaration(1L, DeclarationStatus.ROBOCZE, dt)
        decl.fieldValues = ["POD": ""]  // blank required field
        declarationRepository.findById(1L) >> Optional.of(decl)

        when:
        service.submitDeclaration(1L, USER_ID)

        then:
        def ex = thrown(BusinessException)
        ex.message.contains("POD")
    }

    def "submitDeclaration includes comment in JSON when declaration type has comment and comment is set"() {
        given:
        def dt = buildDeclarationType(true, [])
        def decl = buildDeclaration(1L, DeclarationStatus.ROBOCZE, dt)
        decl.fieldValues = [:]
        decl.comment = "my comment"
        declarationRepository.findById(1L) >> Optional.of(decl)
        declarationRepository.save(decl) >> decl

        when:
        def result = service.submitDeclaration(1L, USER_ID)

        then:
        result["comment"] == "my comment"
    }

    // --- generateDeclarations ---

    def "generateDeclarations creates declarations for each unassigned declaration type"() {
        given:
        def dt = buildDeclarationType()
        def contractorType = new ContractorType(1L, "OSDp", "Operator", false)
        contractorType.declarationTypes = [dt]
        def assignment = new UserContractorTypeAssignment(1L, USER_ID, contractorType)

        assignmentRepository.findByKeycloakUserId(USER_ID) >> Optional.of(assignment)
        declarationRepository.existsByKeycloakUserIdAndDeclarationTypeId(USER_ID, 1L) >> false
        declarationRepository.save(_) >> { Declaration d -> d }
        declarationRepository.findByKeycloakUserIdOrderByCreatedAtDesc(USER_ID) >> []

        when:
        service.generateDeclarations(USER_ID)

        then:
        1 * declarationRepository.save({ Declaration d ->
            d.keycloakUserId == USER_ID &&
            d.status == DeclarationStatus.NIE_ZLOZONE &&
            d.declarationType == dt &&
            d.declarationNumber =~ /OSW\/.+\/.+\/\d{4}\/\d{2}\/\d{2}\/\d{2}\/\d{3}/
        })
    }

    def "generateDeclarations skips declaration types already assigned to user"() {
        given:
        def dt = buildDeclarationType()
        def contractorType = new ContractorType(1L, "OSDp", "Operator", false)
        contractorType.declarationTypes = [dt]
        def assignment = new UserContractorTypeAssignment(1L, USER_ID, contractorType)

        assignmentRepository.findByKeycloakUserId(USER_ID) >> Optional.of(assignment)
        declarationRepository.existsByKeycloakUserIdAndDeclarationTypeId(USER_ID, 1L) >> true
        declarationRepository.findByKeycloakUserIdOrderByCreatedAtDesc(USER_ID) >> []

        when:
        service.generateDeclarations(USER_ID)

        then:
        0 * declarationRepository.save(_)
    }

    def "generateDeclarations throws BusinessException when user has no contractor type assignment"() {
        given:
        assignmentRepository.findByKeycloakUserId(USER_ID) >> Optional.empty()

        when:
        service.generateDeclarations(USER_ID)

        then:
        thrown(BusinessException)
    }

    def "generateDeclarations throws BusinessException when contractor type has no declaration types"() {
        given:
        def contractorType = new ContractorType(1L, "EMPTY", "Empty Type", false)
        contractorType.declarationTypes = []
        def assignment = new UserContractorTypeAssignment(1L, USER_ID, contractorType)
        assignmentRepository.findByKeycloakUserId(USER_ID) >> Optional.of(assignment)

        when:
        service.generateDeclarations(USER_ID)

        then:
        def ex = thrown(BusinessException)
        ex.message.contains("EMPTY")
    }
}
