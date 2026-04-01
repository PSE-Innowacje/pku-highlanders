package pl.pse.pku.declarationtype

import pl.pse.pku.exception.ResourceNotFoundException
import spock.lang.Specification
import spock.lang.Subject

class DeclarationTypeServiceSpec extends Specification {

    DeclarationTypeRepository repository = Mock()

    @Subject
    DeclarationTypeService service = new DeclarationTypeService(repository)

    // --- helpers ---

    private DeclarationType buildType(Long id = 1L, String code = "CODE", boolean hasComment = false,
                                      List<DeclarationTypeField> fields = [],
                                      List<ScheduleEntry> scheduleEntries = []) {
        def dt = new DeclarationType(id, code, "DT Name", "OSDp", hasComment, fields, scheduleEntries)
        fields.each { it.declarationType = dt }
        scheduleEntries.each { it.declarationType = dt }
        dt
    }

    private DeclarationTypeField buildField(String code = "F1", String name = "Field One",
                                            boolean required = true, String unit = "kWh") {
        def f = new DeclarationTypeField()
        f.position = "1"
        f.fieldCode = code
        f.dataType = "NUMBER"
        f.fieldName = name
        f.required = required
        f.unit = unit
        f
    }

    private ScheduleEntry buildScheduleEntry(Long id = null, String position = "1",
                                             int day = 1, int hour = 8, String dayType = "WORKING") {
        def e = new ScheduleEntry()
        e.id = id
        e.position = position
        e.day = day
        e.hour = hour
        e.dayType = dayType
        e
    }

    // --- findAll ---

    def "findAll returns mapped DTOs with field count"() {
        given:
        def field = buildField()
        def dt = buildType(1L, "CODE", true, [field])
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

    def "findAll returns zero fieldCount for type with no fields"() {
        given:
        repository.findAll() >> [buildType()]

        when:
        def result = service.findAll()

        then:
        result[0].fieldCount() == 0
    }

    // --- findByCode ---

    def "findByCode returns detail DTO with fields when code exists"() {
        given:
        def field = buildField("POD", "Punkt Poboru", true, "-")
        field.dataType = "TEXT"
        def dt = buildType(2L, "OP.01", false, [field])
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

    def "findByCode returns detail DTO with empty fields list when type has no fields"() {
        given:
        repository.findByCode("EMPTY") >> Optional.of(buildType(3L, "EMPTY"))

        when:
        def result = service.findByCode("EMPTY")

        then:
        result.fields().isEmpty()
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

    // --- getScheduleEntries ---

    def "getScheduleEntries returns list of schedule entry DTOs for existing code"() {
        given:
        def entry = buildScheduleEntry(1L, "1", 5, 10, "WORKING")
        def dt = buildType(1L, "OP.01", false, [], [entry])
        repository.findByCode("OP.01") >> Optional.of(dt)

        when:
        def result = service.getScheduleEntries("OP.01")

        then:
        result.size() == 1
        with(result[0]) {
            id() == 1L
            position() == "1"
            day() == 5
            hour() == 10
            dayType() == "WORKING"
        }
    }

    def "getScheduleEntries returns empty list when type has no schedule entries"() {
        given:
        repository.findByCode("OP.01") >> Optional.of(buildType())

        when:
        def result = service.getScheduleEntries("OP.01")

        then:
        result.isEmpty()
    }

    def "getScheduleEntries throws ResourceNotFoundException when code does not exist"() {
        given:
        repository.findByCode("MISSING") >> Optional.empty()

        when:
        service.getScheduleEntries("MISSING")

        then:
        def ex = thrown(ResourceNotFoundException)
        ex.message.contains("MISSING")
    }

    // --- saveScheduleEntries ---

    def "saveScheduleEntries replaces existing entries and returns updated list"() {
        given:
        def oldEntry = buildScheduleEntry(1L, "1", 1, 6, "WORKING")
        def dt = buildType(1L, "OP.01", false, [], [oldEntry])
        repository.findByCode("OP.01") >> Optional.of(dt)
        repository.save(dt) >> dt

        def newEntries = [
            new ScheduleEntryDto(null, "1", 3, 8, "WORKING"),
            new ScheduleEntryDto(null, "2", 5, 14, "WEEKEND")
        ]

        when:
        def result = service.saveScheduleEntries("OP.01", newEntries)

        then:
        1 * repository.save(dt)
        dt.scheduleEntries.size() == 2
        dt.scheduleEntries[0].position == "1"
        dt.scheduleEntries[0].day == 3
        dt.scheduleEntries[0].hour == 8
        dt.scheduleEntries[1].dayType == "WEEKEND"
    }

    def "saveScheduleEntries clears all entries when given empty list"() {
        given:
        def existing = buildScheduleEntry(1L, "1", 1, 6, "WORKING")
        def dt = buildType(1L, "OP.01", false, [], [existing])
        repository.findByCode("OP.01") >> Optional.of(dt)
        repository.save(dt) >> dt

        when:
        def result = service.saveScheduleEntries("OP.01", [])

        then:
        dt.scheduleEntries.isEmpty()
        result.isEmpty()
    }

    def "saveScheduleEntries throws ResourceNotFoundException when code does not exist"() {
        given:
        repository.findByCode("MISSING") >> Optional.empty()

        when:
        service.saveScheduleEntries("MISSING", [])

        then:
        def ex = thrown(ResourceNotFoundException)
        ex.message.contains("MISSING")
    }

    def "saveScheduleEntries links each new entry to the declaration type"() {
        given:
        def dt = buildType(1L, "OP.01")
        repository.findByCode("OP.01") >> Optional.of(dt)
        repository.save(dt) >> dt

        when:
        service.saveScheduleEntries("OP.01", [new ScheduleEntryDto(null, "1", 2, 9, "WORKING")])

        then:
        dt.scheduleEntries[0].declarationType == dt
    }
}
