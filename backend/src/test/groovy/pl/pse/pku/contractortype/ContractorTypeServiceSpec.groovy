package pl.pse.pku.contractortype

import pl.pse.pku.declarationtype.DeclarationType
import pl.pse.pku.declarationtype.DeclarationTypeRepository
import pl.pse.pku.exception.BusinessException
import pl.pse.pku.exception.ResourceNotFoundException
import spock.lang.Specification
import spock.lang.Subject

class ContractorTypeServiceSpec extends Specification {

    ContractorTypeRepository repository = Mock()
    DeclarationTypeRepository declarationTypeRepository = Mock()

    @Subject
    ContractorTypeService service = new ContractorTypeService(repository, declarationTypeRepository)

    def "findAll returns list of DTOs with mapped declaration types"() {
        given:
        def type = new ContractorType(1L, "SYM", "Name", false)
        def dt = new DeclarationType(10L, "CODE", "DT Name", "SYM", false, [])
        type.declarationTypes = [dt]
        repository.findAll() >> [type]

        when:
        def result = service.findAll()

        then:
        result.size() == 1
        with(result[0]) {
            id() == 1L
            symbol() == "SYM"
            name() == "Name"
            system() == false
            declarationTypes().size() == 1
            declarationTypes()[0].code() == "CODE"
        }
    }

    def "create saves new entity and returns DTO when symbol is unique"() {
        given:
        def request = new ContractorTypeRequest("NEW", "New Type")
        repository.existsBySymbol("NEW") >> false
        repository.save(_) >> new ContractorType(1L, "NEW", "New Type", false)

        when:
        def result = service.create(request)

        then:
        result.id() == 1L
        result.symbol() == "NEW"
        result.name() == "New Type"
        result.system() == false
    }

    def "create throws BusinessException when symbol already exists"() {
        given:
        repository.existsBySymbol("DUP") >> true

        when:
        service.create(new ContractorTypeRequest("DUP", "Duplicate"))

        then:
        def ex = thrown(BusinessException)
        ex.message.contains("DUP")
    }

    def "update modifies entity and returns DTO on success"() {
        given:
        def entity = new ContractorType(1L, "OLD", "Old Name", false)
        repository.findById(1L) >> Optional.of(entity)
        repository.existsBySymbolAndIdNot("NEW", 1L) >> false
        repository.save(entity) >> entity

        when:
        service.update(1L, new ContractorTypeRequest("NEW", "New Name"))

        then:
        entity.symbol == "NEW"
        entity.name == "New Name"
    }

    def "update throws ResourceNotFoundException when entity does not exist"() {
        given:
        repository.findById(99L) >> Optional.empty()

        when:
        service.update(99L, new ContractorTypeRequest("X", "Y"))

        then:
        thrown(ResourceNotFoundException)
    }

    def "update throws BusinessException when trying to edit a system type"() {
        given:
        repository.findById(1L) >> Optional.of(new ContractorType(1L, "SYS", "System", true))

        when:
        service.update(1L, new ContractorTypeRequest("SYS", "System"))

        then:
        def ex = thrown(BusinessException)
        ex.message.contains("systemowego")
    }

    def "update throws BusinessException when new symbol is already taken by another entity"() {
        given:
        repository.findById(1L) >> Optional.of(new ContractorType(1L, "OLD", "Old", false))
        repository.existsBySymbolAndIdNot("TAKEN", 1L) >> true

        when:
        service.update(1L, new ContractorTypeRequest("TAKEN", "Name"))

        then:
        def ex = thrown(BusinessException)
        ex.message.contains("TAKEN")
    }

    def "delete removes entity when it is not a system type"() {
        given:
        def entity = new ContractorType(1L, "SYM", "Name", false)
        repository.findById(1L) >> Optional.of(entity)

        when:
        service.delete(1L)

        then:
        1 * repository.delete(entity)
    }

    def "delete throws ResourceNotFoundException when entity does not exist"() {
        given:
        repository.findById(99L) >> Optional.empty()

        when:
        service.delete(99L)

        then:
        thrown(ResourceNotFoundException)
    }

    def "delete throws BusinessException when trying to delete a system type"() {
        given:
        repository.findById(1L) >> Optional.of(new ContractorType(1L, "SYS", "System", true))

        when:
        service.delete(1L)

        then:
        def ex = thrown(BusinessException)
        ex.message.contains("systemowego")
    }

    def "updateDeclarationTypes replaces declaration types and returns updated DTO"() {
        given:
        def entity = new ContractorType(1L, "SYM", "Name", false)
        def dt = new DeclarationType(10L, "CODE", "DT Name", "SYM", false, [])
        repository.findById(1L) >> Optional.of(entity)
        declarationTypeRepository.findAllById([10L]) >> [dt]
        repository.save(entity) >> entity

        when:
        def result = service.updateDeclarationTypes(1L, [10L])

        then:
        entity.declarationTypes == [dt]
        result.declarationTypes().size() == 1
        result.declarationTypes()[0].id() == 10L
    }

    def "updateDeclarationTypes throws ResourceNotFoundException when contractor type not found"() {
        given:
        repository.findById(99L) >> Optional.empty()

        when:
        service.updateDeclarationTypes(99L, [1L])

        then:
        thrown(ResourceNotFoundException)
    }

    def "updateDeclarationTypes throws ResourceNotFoundException when some declaration types are missing"() {
        given:
        repository.findById(1L) >> Optional.of(new ContractorType(1L, "SYM", "Name", false))
        declarationTypeRepository.findAllById([1L, 2L]) >> [] // only 0 found, 2 requested

        when:
        service.updateDeclarationTypes(1L, [1L, 2L])

        then:
        thrown(ResourceNotFoundException)
    }
}
