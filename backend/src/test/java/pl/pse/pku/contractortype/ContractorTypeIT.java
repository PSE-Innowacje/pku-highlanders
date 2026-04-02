package pl.pse.pku.contractortype;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import pl.pse.pku.AbstractIT;
import pl.pse.pku.declarationtype.DeclarationTypeRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ContractorTypeIT extends AbstractIT {

    @Autowired
    private ContractorTypeRepository contractorTypeRepository;

    @Autowired
    private DeclarationTypeRepository declarationTypeRepository;

    // GET /api/admin/contractor-types

    @Test
    void getAll_asAdmin_returnsSeededContractorTypes() throws Exception {
        mockMvc.perform(get("/api/admin/contractor-types").with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
            .andExpect(jsonPath("$[*].symbol", hasItems("OSDp", "OSDn", "OK", "Wyt", "Mag")));
    }

    @Test
    void getAll_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/contractor-types"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_asRegularUser_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/contractor-types").with(userJwt("any-user")))
            .andExpect(status().isForbidden());
    }

    // POST /api/admin/contractor-types

    @Test
    void create_asAdmin_createsNewContractorType() throws Exception {
        mockMvc.perform(post("/api/admin/contractor-types")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"symbol": "NEW", "name": "New Type"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.symbol").value("NEW"))
            .andExpect(jsonPath("$.name").value("New Type"))
            .andExpect(jsonPath("$.system").value(false));
    }

    @Test
    void create_duplicateSymbol_returns400() throws Exception {
        mockMvc.perform(post("/api/admin/contractor-types")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"symbol": "OSDp", "name": "Duplicate"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("OSDp")));
    }

    @Test
    void create_blankSymbol_returns400() throws Exception {
        mockMvc.perform(post("/api/admin/contractor-types")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"symbol": "", "name": "No Symbol"}
                    """))
            .andExpect(status().isBadRequest());
    }

    // PUT /api/admin/contractor-types/{id}

    @Test
    void update_nonSystemType_updatesSuccessfully() throws Exception {
        // Create a non-system type to update
        var saved = contractorTypeRepository.saveAndFlush(
            new ContractorType(null, "UPD", "Original", false));

        mockMvc.perform(put("/api/admin/contractor-types/" + saved.getId())
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"symbol": "UPD", "name": "Updated Name"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void update_systemType_returns400() throws Exception {
        var systemType = contractorTypeRepository.findAll().stream()
            .filter(ContractorType::isSystem)
            .findFirst().orElseThrow();

        mockMvc.perform(put("/api/admin/contractor-types/" + systemType.getId())
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"symbol": "NEW", "name": "Attempt"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("systemowego")));
    }

    @Test
    void update_nonExistentId_returns404() throws Exception {
        mockMvc.perform(put("/api/admin/contractor-types/99999")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"symbol": "X", "name": "Y"}
                    """))
            .andExpect(status().isNotFound());
    }

    // DELETE /api/admin/contractor-types/{id}

    @Test
    void delete_nonSystemType_returns204() throws Exception {
        var saved = contractorTypeRepository.saveAndFlush(
            new ContractorType(null, "DEL", "To Delete", false));

        mockMvc.perform(delete("/api/admin/contractor-types/" + saved.getId())
                .with(adminJwt()))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_systemType_returns400() throws Exception {
        var systemType = contractorTypeRepository.findAll().stream()
            .filter(ContractorType::isSystem)
            .findFirst().orElseThrow();

        mockMvc.perform(delete("/api/admin/contractor-types/" + systemType.getId())
                .with(adminJwt()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("systemowego")));
    }

    // PUT /api/admin/contractor-types/{id}/declaration-types

    @Test
    void updateDeclarationTypes_associatesDeclarationTypes() throws Exception {
        var contractorType = contractorTypeRepository.saveAndFlush(
            new ContractorType(null, "ASSOC", "Assoc Type", false));
        var declarationType = declarationTypeRepository.findByCode("OZE.c").orElseThrow();

        mockMvc.perform(put("/api/admin/contractor-types/" + contractorType.getId() + "/declaration-types")
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"declarationTypeIds": [%d]}
                    """.formatted(declarationType.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.declarationTypes", hasSize(1)))
            .andExpect(jsonPath("$.declarationTypes[0].code").value("OZE.c"));
    }
}
