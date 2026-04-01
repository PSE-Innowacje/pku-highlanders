package pl.pse.pku.declarationtype

import pl.pse.pku.exception.ResourceNotFoundException
import spock.lang.Specification
import spock.lang.Subject

class DeclarationTypeServiceSpec extends Specification {

    def repository = Mock(DeclarationTypeRepository)

    @Subject
    def service = new DeclarationTypeService(repository)

    // --- findAll ---

    def "findAll returns all declaration types"() {
        given:
        def dt1 = makeDeclarationType(1L, "OP.OSDp", "Opłata przejściowa", "OSDp", true, 5)
        def dt2 = makeDeclarationType(2L, "OP.OK", "Opłata OK", "OK", false, 3)
        repository.findAll() >> [dt1, dt2]

        when:
        def result = service.findAll()

        then:
        result.size() == 2
        result[0].code() == "OP.OSDp"
        result[0].fieldCount() == 5
        result[1].code() == "OP.OK"
        result[1].hasComment() == false
    }

    def "findAll returns empty list when no types exist"() {
        given:
        repository.findAll() >> []

        when:
        def result = service.findAll()

        then:
        result.isEmpty()
    }

    // --- findByCode ---

    def "findByCode returns declaration type detail with fields"() {
        given:
        def dt = makeDeclarationType(1L, "OP.OSDp", "Opłata przejściowa", "OSDp", true, 0)
        def field = new DeclarationTypeField()
        field.position = "1"
        field.fieldCode = "F1"
        field.dataType = "Number (9,3)"
        field.fieldName = "Pole testowe"
        field.required = true
        field.unit = "kW"
        dt.fields = [field]
        repository.findByCode("OP.OSDp") >> Optional.of(dt)

        when:
        def result = service.findByCode("OP.OSDp")

        then:
        result.code() == "OP.OSDp"
        result.fields().size() == 1
        result.fields()[0].fieldCode() == "F1"
        result.fields()[0].dataType() == "Number (9,3)"
    }

    def "findByCode throws when code not found"() {
        given:
        repository.findByCode("UNKNOWN") >> Optional.empty()

        when:
        service.findByCode("UNKNOWN")

        then:
        thrown(ResourceNotFoundException)
    }

    // --- getScheduleEntries ---

    def "getScheduleEntries returns entries for declaration type"() {
        given:
        def dt = makeDeclarationType(1L, "OP.OSDp", "Test", "OSDp", false, 0)
        def entry = new ScheduleEntry()
        entry.id = 1L
        entry.position = "Składanie oświadczenia rozliczeniowego"
        entry.day = 15
        entry.hour = 12
        entry.dayType = "Dzień kalendarzowy"
        entry.declarationType = dt
        dt.scheduleEntries = [entry]

        repository.findByCode("OP.OSDp") >> Optional.of(dt)

        when:
        def result = service.getScheduleEntries("OP.OSDp")

        then:
        result.size() == 1
        result[0].day() == 15
        result[0].hour() == 12
        result[0].position() == "Składanie oświadczenia rozliczeniowego"
    }

    // --- saveScheduleEntries ---

    def "saveScheduleEntries replaces all entries"() {
        given:
        def dt = makeDeclarationType(1L, "OP.OSDp", "Test", "OSDp", false, 0)
        def oldEntry = new ScheduleEntry()
        oldEntry.id = 1L
        oldEntry.declarationType = dt
        dt.scheduleEntries = new ArrayList([oldEntry])

        def newEntries = [
            new ScheduleEntryDto(null, "Składanie oświadczenia rozliczeniowego", 1, 8, "Dzień kalendarzowy"),
            new ScheduleEntryDto(null, "Wystawienie faktury za świadczenie usług", 15, 12, "Dzień roboczy"),
        ]

        repository.findByCode("OP.OSDp") >> Optional.of(dt)
        repository.save(dt) >> dt

        when:
        def result = service.saveScheduleEntries("OP.OSDp", newEntries)

        then:
        result.size() == 2
        dt.scheduleEntries.size() == 2
        dt.scheduleEntries[0].day == 1
        dt.scheduleEntries[1].day == 15
    }

    def "saveScheduleEntries clears entries when empty list"() {
        given:
        def dt = makeDeclarationType(1L, "OP.OSDp", "Test", "OSDp", false, 0)
        dt.scheduleEntries = new ArrayList()

        repository.findByCode("OP.OSDp") >> Optional.of(dt)
        repository.save(dt) >> dt

        when:
        def result = service.saveScheduleEntries("OP.OSDp", [])

        then:
        result.isEmpty()
        dt.scheduleEntries.isEmpty()
    }

    // --- helpers ---

    private DeclarationType makeDeclarationType(Long id, String code, String name, String contractorTypes, boolean hasComment, int fieldCount) {
        def dt = new DeclarationType()
        dt.id = id
        dt.code = code
        dt.name = name
        dt.contractorTypes = contractorTypes
        dt.hasComment = hasComment
        dt.fields = (1..fieldCount).collect {
            def f = new DeclarationTypeField()
            f.position = String.valueOf(it)
            f.fieldCode = "F${it}"
            f.dataType = "Number"
            f.fieldName = "Field ${it}"
            f.required = false
            f.unit = "szt"
            return f
        }
        dt.scheduleEntries = []
        return dt
    }
}
