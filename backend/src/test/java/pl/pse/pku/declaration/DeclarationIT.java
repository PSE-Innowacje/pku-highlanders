package pl.pse.pku.declaration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import pl.pse.pku.AbstractIT;
import pl.pse.pku.contractortype.ContractorType;
import pl.pse.pku.contractortype.ContractorTypeRepository;
import pl.pse.pku.declarationtype.DeclarationTypeRepository;
import pl.pse.pku.userassignment.UserContractorTypeAssignment;
import pl.pse.pku.userassignment.UserContractorTypeAssignmentRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DeclarationIT extends AbstractIT {

    private static final String USER_ID = "kontrahent-user-uuid-1";

    @Autowired
    private ContractorTypeRepository contractorTypeRepository;
    @Autowired
    private DeclarationTypeRepository declarationTypeRepository;
    @Autowired
    private UserContractorTypeAssignmentRepository assignmentRepository;
    @Autowired
    private DeclarationRepository declarationRepository;

    private ContractorType contractorType;

    @BeforeEach
    void setUp() {
        // Attach OZE.c (single required field "OZEil") to the OSDp contractor type
        contractorType = contractorTypeRepository.findAll().stream()
            .filter(ct -> "OSDp".equals(ct.getSymbol()))
            .findFirst().orElseThrow();

        var declarationType = declarationTypeRepository.findByCode("OZE.c").orElseThrow();
        contractorType.getDeclarationTypes().add(declarationType);
        contractorTypeRepository.saveAndFlush(contractorType);

        // Assign the contractor type to the test user
        assignmentRepository.saveAndFlush(
            new UserContractorTypeAssignment(null, USER_ID, contractorType));
    }

    // POST /api/declarations/generate

    @Test
    void generate_createsDeclarationsForAssignedContractorType() throws Exception {
        mockMvc.perform(post("/api/declarations/generate").with(userJwt(USER_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].declarationTypeCode").value("OZE.c"))
            .andExpect(jsonPath("$[0].status").value("NIE_ZLOZONE"))
            .andExpect(jsonPath("$[0].declarationNumber", matchesPattern("OSW/.+/.+/\\d{4}/\\d{2}/\\d{2}/\\d{2}/\\d{3}")));
    }

    @Test
    void generate_calledTwice_doesNotDuplicate() throws Exception {
        mockMvc.perform(post("/api/declarations/generate").with(userJwt(USER_ID)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/declarations/generate").with(userJwt(USER_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));  // still exactly 1, not 2
    }

    @Test
    void generate_userWithNoAssignment_returns400() throws Exception {
        mockMvc.perform(post("/api/declarations/generate").with(userJwt("unassigned-user")))
            .andExpect(status().isBadRequest());
    }

    // GET /api/declarations

    @Test
    void getMyDeclarations_returnsUserDeclarations() throws Exception {
        var decl = createDeclaration(DeclarationStatus.NIE_ZLOZONE, Map.of());

        mockMvc.perform(get("/api/declarations").with(userJwt(USER_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(decl.getId()))
            .andExpect(jsonPath("$[0].status").value("NIE_ZLOZONE"));
    }

    @Test
    void getMyDeclarations_doesNotReturnOtherUsersDeclarations() throws Exception {
        createDeclaration(DeclarationStatus.NIE_ZLOZONE, Map.of());  // USER_ID's declaration

        mockMvc.perform(get("/api/declarations").with(userJwt("another-user")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getMyDeclarations_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/declarations"))
            .andExpect(status().isUnauthorized());
    }

    // GET /api/declarations/{id}

    @Test
    void getDeclarationDetail_returnsDetailWithFields() throws Exception {
        var decl = createDeclaration(DeclarationStatus.NIE_ZLOZONE, Map.of());

        mockMvc.perform(get("/api/declarations/" + decl.getId()).with(userJwt(USER_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(decl.getId()))
            .andExpect(jsonPath("$.declarationTypeCode").value("OZE.c"))
            .andExpect(jsonPath("$.fields", hasSize(1)))
            .andExpect(jsonPath("$.fields[0].fieldCode").value("OZEil"));
    }

    @Test
    void getDeclarationDetail_otherUsersDeclaration_returns400() throws Exception {
        var decl = createDeclaration(DeclarationStatus.NIE_ZLOZONE, Map.of());

        mockMvc.perform(get("/api/declarations/" + decl.getId()).with(userJwt("intruder")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getDeclarationDetail_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/declarations/99999").with(userJwt(USER_ID)))
            .andExpect(status().isNotFound());
    }

    // PUT /api/declarations/{id}

    @Test
    void saveDeclaration_updatesFieldValuesAndSetsRoboczStatus() throws Exception {
        var decl = createDeclaration(DeclarationStatus.NIE_ZLOZONE, Map.of());

        mockMvc.perform(put("/api/declarations/" + decl.getId())
                .with(userJwt(USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fieldValues": {"OZEil": "123.45"}, "comment": "test comment"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ROBOCZE"))
            .andExpect(jsonPath("$.fieldValues.OZEil").value("123.45"))
            .andExpect(jsonPath("$.comment").value("test comment"));
    }

    @Test
    void saveDeclaration_alreadySubmitted_returns400() throws Exception {
        var decl = createDeclaration(DeclarationStatus.ZLOZONE, Map.of("OZEil", "100"));

        mockMvc.perform(put("/api/declarations/" + decl.getId())
                .with(userJwt(USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fieldValues": {"OZEil": "999"}, "comment": null}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Złożone")));
    }

    // POST /api/declarations/{id}/submit

    @Test
    void submitDeclaration_withAllRequiredFields_transitionsToZlozone() throws Exception {
        var decl = createDeclaration(DeclarationStatus.ROBOCZE, Map.of("OZEil", "250.5"));

        mockMvc.perform(post("/api/declarations/" + decl.getId() + "/submit")
                .with(userJwt(USER_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("Złożone"))
            .andExpect(jsonPath("$.declarationNumber").value(decl.getDeclarationNumber()))
            .andExpect(jsonPath("$.fields.OZEil.value").value("250.5"));
    }

    @Test
    void submitDeclaration_notInRoboczStatus_returns400() throws Exception {
        var decl = createDeclaration(DeclarationStatus.NIE_ZLOZONE, Map.of());

        mockMvc.perform(post("/api/declarations/" + decl.getId() + "/submit")
                .with(userJwt(USER_ID)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Robocze")));
    }

    @Test
    void submitDeclaration_missingRequiredField_returns400() throws Exception {
        var decl = createDeclaration(DeclarationStatus.ROBOCZE, Map.of());  // OZEil is missing

        mockMvc.perform(post("/api/declarations/" + decl.getId() + "/submit")
                .with(userJwt(USER_ID)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("OZEil")));
    }

    // --- helpers ---

    private Declaration createDeclaration(DeclarationStatus status, Map<String, String> fieldValues) {
        var dt = declarationTypeRepository.findByCode("OZE.c").orElseThrow();
        var d = new Declaration();
        d.setDeclarationNumber("OSW/OZE/OSDp/2025/01/01/01/" + System.nanoTime() % 1000);
        d.setStatus(status);
        d.setKeycloakUserId(USER_ID);
        d.setDeclarationType(dt);
        d.setCreatedAt(LocalDateTime.now());
        d.setFieldValues(new HashMap<>(fieldValues));
        return declarationRepository.saveAndFlush(d);
    }
}
