package com.javahood.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RbacAndExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetAccessToken(String username, String password) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(res.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();
    }

    @Test
    void rbacEnforcesPermissionsAndCsvExportNeutralizesFormulas() throws Exception {
        String viewerToken = loginAndGetAccessToken("viewer", "view123");
        String operatorToken = loginAndGetAccessToken("operator", "oper123");
        String adminToken = loginAndGetAccessToken("admin", "admin123");

        // Viewer can read paginated list
        mockMvc.perform(get("/api/users?page=0&size=2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2));

        // Viewer cannot export or create
        mockMvc.perform(get("/api/users/export")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + viewerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"secret123\",\"displayName\":\"New\",\"email\":\"new@javahood.dev\",\"roleCodes\":[\"ROLE_VIEWER\"]}"))
                .andExpect(status().isForbidden());

        // Admin creates a user with a formula-like displayName
        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice_test\",\"password\":\"secret123\",\"displayName\":\"=SUM(1,2)\",\"email\":\"alice@javahood.dev\",\"roleCodes\":[\"ROLE_VIEWER\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice_test"));

        // Operator exports CSV and verifies UTF-8 BOM + formula neutralization
        MvcResult csvRes = mockMvc.perform(get("/api/users/export?keyword=alice_test")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andReturn();

        String csvBody = csvRes.getResponse().getContentAsString();
        assertThat(csvBody).startsWith("\uFEFFID,Username,DisplayName");
        assertThat(csvBody).contains("alice_test");
        assertThat(csvBody).contains("\"'=SUM(1,2)\"");
    }
}
