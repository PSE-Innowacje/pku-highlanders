package pl.pse.pku.seeder;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pl.pse.pku.contractordata.ContractorData;
import pl.pse.pku.contractordata.ContractorDataRepository;
import pl.pse.pku.contractortype.ContractorType;
import pl.pse.pku.contractortype.ContractorTypeRepository;
import pl.pse.pku.keycloak.KeycloakAdminService;
import pl.pse.pku.userassignment.UserContractorTypeAssignment;
import pl.pse.pku.userassignment.UserContractorTypeAssignmentRepository;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class ContractorDataSeeder implements ApplicationRunner {

    private final ContractorDataRepository contractorDataRepository;
    private final ContractorTypeRepository contractorTypeRepository;
    private final UserContractorTypeAssignmentRepository assignmentRepository;
    private final KeycloakAdminService keycloakAdminService;

    private static final String[] FIRST_NAMES = {
        "Jan", "Anna", "Piotr", "Maria", "Tomasz", "Katarzyna", "Andrzej", "Małgorzata",
        "Krzysztof", "Agnieszka", "Marcin", "Barbara", "Paweł", "Ewa", "Michał",
        "Joanna", "Marek", "Dorota", "Robert", "Monika", "Łukasz", "Beata",
        "Adam", "Iwona", "Jakub", "Aleksandra", "Grzegorz", "Magdalena", "Rafał"
    };

    private static final String[] LAST_NAMES = {
        "Kowalski", "Nowak", "Wiśniewski", "Wójcik", "Kowalczyk", "Kamiński", "Lewandowski",
        "Zieliński", "Szymański", "Woźniak", "Dąbrowski", "Kozłowski", "Jankowski",
        "Mazur", "Kwiatkowski", "Krawczyk", "Piotrowski", "Grabowski", "Nowakowski",
        "Pawłowski", "Michalski", "Nowicki", "Adamczyk", "Dudek", "Zając", "Wieczorek",
        "Jabłoński", "Król", "Majewski"
    };

    private static final String[] CITIES = {
        "Warszawa", "Kraków", "Łódź", "Wrocław", "Poznań", "Gdańsk", "Szczecin",
        "Bydgoszcz", "Lublin", "Białystok", "Katowice", "Gdynia", "Częstochowa",
        "Radom", "Sosnowiec", "Toruń", "Kielce", "Rzeszów", "Gliwice", "Zabrze",
        "Olsztyn", "Bielsko-Biała", "Bytom", "Zielona Góra", "Rybnik", "Ruda Śląska",
        "Tychy", "Opole", "Płock"
    };

    record TestContractor(String agreementNumber, String typeString, String fullName, String abbreviation) {}

    private static final List<TestContractor> TEST_DATA = List.of(
        new TestContractor("UPE/OKp-OSDp-PWn/JSWK/2024", "OSDp OK", "JSW KOKS Spółka Akcyjna", "JSWK"),
        new TestContractor("UPE/OKp-OSDp-PWn/ANWK/2024", "OSDp OK", "ANWIL Spółka Akcyjna", "ANWK"),
        new TestContractor("UPE/OKp-OSDp-PWn/ZCHK/2024", "OSDp OK", "Grupa Azoty Zakłady Chemiczne \"Police\" Spółka Akcyjna", "ZCHK"),
        new TestContractor("UPE/OSDp/KEED/2016", "OSDp", "ENERGA-OPERATOR S.A.", "KEED"),
        new TestContractor("UPE/OKp/STOK/2024", "OK", "Stora Enso Poland Spółka Akcyjna", "STOK"),
        new TestContractor("UPE/OKp/CMCK/2024", "OK", "CMC Poland Spółka z ograniczoną odpowiedzialnością", "CMCK"),
        new TestContractor("UPE/OKp/KWSA/2007", "OK", "Polska Grupa Górnicza spółka akcyjna", "KWSA"),
        new TestContractor("UPE/FWp/MARG/2024", "WYT", "Relax Wind Park I Spółka z ograniczoną odpowiedzialnością", "MARG"),
        new TestContractor("UPE/WEp/BIWW/2024", "WYT", "PAK-PCE BIOPALIWA i WODÓR Spółka z ograniczoną odpowiedzialnością", "BIWW"),
        new TestContractor("UPE/PVp/NEOS/2024", "WYT", "Neo Solar Farms Spółka z ograniczoną odpowiedzialnością", "NEOS"),
        new TestContractor("UPE/FWp/SLUP/2024", "WYT", "Green Power Pomorze Spółka z ograniczoną odpowiedzialnością", "SLUP"),
        new TestContractor("UPE/FWp-PVp/EIGW/2024", "WYT", "E&G Spółka z ograniczoną odpowiedzialnością", "EIGW"),
        new TestContractor("UPE/WEp/SKAW/2024", "WYT", "ResInvest Energy Skawina Spółka Akcyjna", "SKAW"),
        new TestContractor("UPE/FWp/BBOR/2024", "WYT", "Biały Bór Farma Wiatrowa Spółka z ograniczoną odpowiedzialnością", "BBOR"),
        new TestContractor("UPE/OSDp-PWn/PGEK/2024", "OSDp", "PGE Energetyka Kolejowa Obsługa spółka z ograniczoną odpowiedzialnością", "PGEK"),
        new TestContractor("UPE/OSDp-PWn/KORD/2024", "OSDp", "Wind Field Korytnica Spółka z ograniczoną odpowiedzialnością", "KORD"),
        new TestContractor("UPE/OSDp-PWn/GRFD/2024", "OSDp", "Fieldon Investments Spółka z ograniczoną odpowiedzialnością GRYF Spółka jawna", "GRFD"),
        new TestContractor("UPE/OSDp-PWn/WSDD/2024", "OSDp", "Wind Service Dystrybucja Spółka z ograniczoną odpowiedzialnością", "WSDD"),
        new TestContractor("UPE/OSDp-PWn/RAMD/2024", "OSDp", "Rampton spółka z ograniczoną odpowiedzialnością", "RAMD"),
        new TestContractor("UKDT/OSD/BORYSZEWERG/2012", "OSDn", "Boryszew Spółka Akcyjna Oddział Boryszew ERG w Sochaczewie", "BORYSZEWERG"),
        new TestContractor("UKDT/OSD/BLACHOWNIA/2012", "OSDn", "PCC Energetyka Blachownia Spółka z ograniczoną odpowiedzialnością", "BLACHOWNIA"),
        new TestContractor("UKDT/OSD/BHH/2011", "OSDn", "BHH MIKROHUTA Spółka z ograniczoną odpowiedzialnością", "BHH"),
        new TestContractor("UKDT/OSD/BDSO/2014", "OSDn", "Przedsiębiorstwo Handlowo Usługowe BRODZIK Spółka Jawna", "BDSO"),
        new TestContractor("UKDT/OSD/BD/2011", "OSDn", "BD Spółka z ograniczoną odpowiedzialnością", "BD"),
        new TestContractor("UKDT/OSD/ARCTIC/2011", "OSDn", "ARCTIC PAPER KOSTRZYN SPÓŁKA AKCYJNA", "ARCTIC"),
        new TestContractor("UKDT/OSD/AMP/2013", "OSDn", "ArcelorMittal Poland Spółka Akcyjna", "AMP"),
        new TestContractor("UKDT/OSD/ADIPOL/2011", "OSDn", "Grupa Azoty Zakłady Azotowe Chorzów S.A.", "ADIPOL"),
        new TestContractor("UPE/MEp-MEd-FWp/PGEO/2024", "MAG", "PGE Energia Odnawialna Spółka Akcyjna", "PGEO")
    );

    @Override
    public void run(ApplicationArguments args) {
        if (contractorDataRepository.count() > 0) {
            return;
        }

        int created = 0;
        for (int i = 0; i < TEST_DATA.size(); i++) {
            var tc = TEST_DATA.get(i);
            try {
                String username = tc.abbreviation().toLowerCase() + "@pku.pl";
                String firstName = FIRST_NAMES[i % FIRST_NAMES.length];
                String lastName = LAST_NAMES[i % LAST_NAMES.length];
                String email = username;

                // Create user in Keycloak if not exists
                String keycloakUserId;
                if (!keycloakAdminService.userExists(username)) {
                    keycloakUserId = keycloakAdminService.createUser(username, email, firstName, lastName, "test");
                    keycloakAdminService.assignRealmRole(keycloakUserId, "Kontrahent");
                    log.info("Created Keycloak user: {}", username);
                } else {
                    // User already exists, find their ID
                    var users = keycloakAdminService.getKontrahentUsers();
                    keycloakUserId = users.stream()
                        .filter(u -> u.username().equals(username))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("User exists but not found: " + username))
                        .id();
                }

                // Create ContractorData
                var data = new ContractorData();
                data.setKeycloakUserId(keycloakUserId);
                data.setContractorAbbreviation(tc.abbreviation());
                data.setContractorFullName(tc.fullName());
                data.setContractorShortName(tc.fullName().length() > 50 ? tc.fullName().substring(0, 50) : tc.fullName());
                data.setKrs(generateKrs());
                data.setNip(generateNip());
                data.setRegisteredAddress(generateAddress(i));
                data.setContractorCode(tc.abbreviation());
                data.setAgreementNumber(tc.agreementNumber());
                data.setAgreementDateFrom(LocalDate.of(2024, 1, 1));
                data.setAgreementDateTo(LocalDate.of(2025, 12, 31));
                contractorDataRepository.save(data);

                // Create type assignments
                String[] typeSymbols = tc.typeString().split("\\s+");
                for (String symbol : typeSymbols) {
                    contractorTypeRepository.findBySymbolIgnoreCase(symbol.trim())
                        .ifPresent(type -> assignmentRepository.save(
                            new UserContractorTypeAssignment(null, keycloakUserId, type)));
                }

                created++;
            } catch (Exception e) {
                log.warn("Failed to seed contractor {}: {}", tc.abbreviation(), e.getMessage());
            }
        }
        log.info("Seeded {} contractor data records with Keycloak users", created);
    }

    private String generateKrs() {
        return String.format("%010d", ThreadLocalRandom.current().nextLong(1_000_000_000L));
    }

    private String generateNip() {
        return String.format("%010d", ThreadLocalRandom.current().nextLong(1_000_000_000L));
    }

    private String generateAddress(int index) {
        String city = CITIES[index % CITIES.length];
        int streetNum = ThreadLocalRandom.current().nextInt(1, 200);
        return "ul. Przemysłowa " + streetNum + ", " + city;
    }
}
