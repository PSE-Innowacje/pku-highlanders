package pl.pse.pku.userassignment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import pl.pse.pku.AbstractIT;
import pl.pse.pku.contractortype.ContractorType;
import pl.pse.pku.contractortype.ContractorTypeRepository;
import pl.pse.pku.keycloak.KeycloakUserDto;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserAssignmentIT extends AbstractIT {

    private static final String USER_ID  = "keycloak-user-uuid-1";
    private static final String USER2_ID = "keycloak-user-uuid-2";

    @Autowired
    private ContractorTypeRepository contractorTypeRepository;
    @Autowired
    private UserContractorTypeAssignmentRepository assignmentRepository;

    // GET /api/admin/kontrahent-users

    @Test
    void listUsers_asAdmin_returnsUsersWithAssignedTypes() throws Exception {
        var contractorType = seededType("OSDp");
        assignmentRepository.saveAndFlush(new UserContractorTypeAssignment(null, USER_ID, contractorType));

        when(keycloakAdminService.getKontrahentUsers()).thenReturn(List.of(
            new KeycloakUserDto(USER_ID, "jkowalski", "Jan", "Kowalski", "jan@example.com"),
            new KeycloakUserDto(USER2_ID, "anowak", "Anna", "Nowak", "anna@example.com")
        ));

        mockMvc.perform(get("/api/admin/kontrahent-users").with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[?(@.keycloakUserId == '%s')].assignedTypes[0].symbol".formatted(USER_ID))
                .value(hasItem("OSDp")))
            .andExpect(jsonPath("$[?(@.keycloakUserId == '%s')].assignedTypes".formatted(USER2_ID),
                contains(empty())));
    }

    @Test
    void listUsers_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/kontrahent-users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listUsers_asRegularUser_returns403() throws Exception {
        when(keycloakAdminService.getKontrahentUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/kontrahent-users").with(userJwt("any")))
            .andExpect(status().isForbidden());
    }

    // PUT /api/admin/kontrahent-users/{userId}/contractor-types

    @Test
    void updateAssignment_createsNewAssignment() throws Exception {
        var contractorType = seededType("OSDp");
        when(keycloakAdminService.getKontrahentUsers()).thenReturn(List.of(
            new KeycloakUserDto(USER_ID, "jkowalski", "Jan", "Kowalski", "jan@example.com")
        ));

        mockMvc.perform(put("/api/admin/kontrahent-users/%s/contractor-types".formatted(USER_ID))
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"contractorTypeIds": [%d]}
                    """.formatted(contractorType.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.keycloakUserId").value(USER_ID))
            .andExpect(jsonPath("$.assignedTypes[0].symbol").value("OSDp"));
    }

    @Test
    void updateAssignment_updatesExistingAssignment() throws Exception {
        var osdp = seededType("OSDp");
        var ok   = seededType("OK");
        assignmentRepository.saveAndFlush(new UserContractorTypeAssignment(null, USER_ID, osdp));

        when(keycloakAdminService.getKontrahentUsers()).thenReturn(List.of(
            new KeycloakUserDto(USER_ID, "jkowalski", "Jan", "Kowalski", "jan@example.com")
        ));

        mockMvc.perform(put("/api/admin/kontrahent-users/%s/contractor-types".formatted(USER_ID))
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"contractorTypeIds": [%d]}
                    """.formatted(ok.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assignedTypes[0].symbol").value("OK"));
    }

    @Test
    void updateAssignment_emptyList_removesAllAssignments() throws Exception {
        var osdp = seededType("OSDp");
        assignmentRepository.saveAndFlush(new UserContractorTypeAssignment(null, USER_ID, osdp));

        when(keycloakAdminService.getKontrahentUsers()).thenReturn(List.of(
            new KeycloakUserDto(USER_ID, "jkowalski", "Jan", "Kowalski", "jan@example.com")
        ));

        mockMvc.perform(put("/api/admin/kontrahent-users/%s/contractor-types".formatted(USER_ID))
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"contractorTypeIds": []}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assignedTypes", empty()));
    }

    @Test
    void updateAssignment_userNotInKeycloak_returns404() throws Exception {
        when(keycloakAdminService.getKontrahentUsers()).thenReturn(List.of());

        mockMvc.perform(put("/api/admin/kontrahent-users/%s/contractor-types".formatted(USER_ID))
                .with(adminJwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"contractorTypeIds": []}
                    """))
            .andExpect(status().isNotFound());
    }

    // --- helpers ---

    private ContractorType seededType(String symbol) {
        return contractorTypeRepository.findAll().stream()
            .filter(ct -> symbol.equals(ct.getSymbol()))
            .findFirst().orElseThrow();
    }
}
