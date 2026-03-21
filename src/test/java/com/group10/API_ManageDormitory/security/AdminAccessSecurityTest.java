package com.group10.API_ManageDormitory.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
public class AdminAccessSecurityTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_OWNER")
    void givenOwner_whenAccessOwnerEndpoint_thenNotForbidden() throws Exception {
        // Tenant export is PreAuthorize("hasAuthority('SCOPE_OWNER')")
        mockMvc.perform(get("/api/v1/tenants/export-police"))
               .andExpect(result -> {
                   if (result.getResponse().getStatus() == 403) {
                       throw new AssertionError("Expected any status other than 403 Forbidden");
                   }
               });
    }

    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void givenAdmin_whenAccessOwnerEndpoint_thenNotForbidden() throws Exception {
        // Even though export is PreAuthorize("hasAuthority('SCOPE_OWNER')"), ADMIN should have access via RoleHierarchy
        mockMvc.perform(get("/api/v1/tenants/export-police"))
               .andExpect(result -> {
                   if (result.getResponse().getStatus() == 403) {
                       throw new AssertionError("Expected any status other than 403 Forbidden");
                   }
               });
    }

    @Test
    @WithMockUser(authorities = "SCOPE_STAFF")
    void givenStaff_whenAccessOwnerEndpoint_thenForbidden() throws Exception {
        // Staff should be forbidden from Owner only endpoint
        mockMvc.perform(get("/api/v1/tenants/export-police"))
               .andExpect(status().isForbidden());
    }

    // Checking another method (createAsset requires SCOPE_OWNER)
    @Test
    @WithMockUser(authorities = "SCOPE_ADMIN")
    void givenAdmin_whenPostOwnerEndpoint_thenNotForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/assets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Asset\",\"amount\":1}"))
               .andExpect(result -> {
                   if (result.getResponse().getStatus() == 403) {
                       throw new AssertionError("Expected any status other than 403 Forbidden for Admin on Owner resources");
                   }
               });
    }
}
