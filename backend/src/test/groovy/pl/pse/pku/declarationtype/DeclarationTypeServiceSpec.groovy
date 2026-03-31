package pl.pse.pku.declarationtype

import pl.pse.pku.exception.ResourceNotFoundException
import spock.lang.Specification
import spock.lang.Subject

class DeclarationTypeServiceSpec extends Specification {

    DeclarationTypeRepository repository = Mock()

    @Subject
    DeclarationTypeService service = new DeclarationTypeService(repository)

    def "findAll returns mapped DTOs with field count"() {
        given:
        def field = new DeclarationTypeField()
        field.position = "1"
        field.fieldCode = "F1"
        field.dataType = "NUMBER"
        field.fieldName = "Field One"
        field.required = true
        field.unit = "kWh"

        def dt = new DeclarationType(1L, "CODE", "DT Name", "OSDp", true, [field])
        field.declarationType = dt
        repository.findAll() >> [dt]

        when:
        def result = service.findAll()

        then:
        result.size() == 1
        with(result[0]) {
            id() == 1L
            code() == "CODE"
            name() == "DT Name"
            hasComment() == true
            fieldCount() == 1
        }
    }

    def "findAll returns empty list when no declaration types exist"() {
        given:
        repository.findAll() >> []

        when:
        def result = service.findAll()

        then:
        result.isEmpty()
    }

    def "findByCode returns detail DTO with fields when code exists"() {
        given:
        def field = new DeclarationTypeField()
        field.position = "1"
        field.fieldCode = "POD"
        field.dataType = "TEXT"
        field.fieldName = "Punkt Poboru"
        field.required = true
        field.unit = "-"

        def dt = new DeclarationType(2L, "OP.01", "Oświadczenie", "OSDp", false, [field])
        field.declarationType = dt
        repository.findByCode("OP.01") >> Optional.of(dt)

        when:
        def result = service.findByCode("OP.01")

        then:
        result.id() == 2L
        result.code() == "OP.01"
        result.fields().size() == 1
        with(result.fields()[0]) {
            fieldCode() == "POD"
            fieldName() == "Punkt Poboru"
            required() == true
        }
    }

    def "findByCode throws ResourceNotFoundException when code does not exist"() {
        given:
        repository.findByCode("MISSING") >> Optional.empty()

        when:
        service.findByCode("MISSING")

        then:
        def ex = thrown(ResourceNotFoundException)
        ex.message.contains("MISSING")
    }
}
