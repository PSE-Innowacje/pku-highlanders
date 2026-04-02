package pl.pse.pku.declarationtype;

import org.junit.jupiter.api.Test;
import pl.pse.pku.AbstractIT;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DeclarationTypeIT extends AbstractIT {

    // GET /api/admin/declaration-types

    @Test
    void getAll_asAdmin_returnsAllSeededDeclarationTypes() throws Exception {
        mockMvc.perform(get("/api/admin/declaration-types").with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(15)))
            .andExpect(jsonPath("$[*].code", hasItems("OP.a", "OZE.b", "OZE.c", "OM.g", "OJ.i")));
    }

    @Test
    void getAll_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/declaration-types"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_asRegularUser_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/declaration-types").with(userJwt("any-user")))
            .andExpect(status().isForbidden());
    }

    // GET /api/admin/declaration-types/{code}

    @Test
    void getByCode_existingCode_returnsDetailWithFields() throws Exception {
        mockMvc.perform(get("/api/admin/declaration-types/OZE.c").with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("OZE.c"))
            .andExpect(jsonPath("$.name").value("Opłata OZE"))
            .andExpect(jsonPath("$.fields", hasSize(1)))
            .andExpect(jsonPath("$.fields[0].fieldCode").value("OZEil"))
            .andExpect(jsonPath("$.fields[0].required").value(true));
    }

    @Test
    void getByCode_complexType_returnsAllFields() throws Exception {
        mockMvc.perform(get("/api/admin/declaration-types/OP.a").with(adminJwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("OP.a"))
            .andExpect(jsonPath("$.fields", hasSize(9)));
    }

    @Test
    void getByCode_nonExistentCode_returns404() throws Exception {
        mockMvc.perform(get("/api/admin/declaration-types/MISSING").with(adminJwt()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("MISSING")));
    }
}
