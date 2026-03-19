package com.group10.API_ManageDormitory.integration;

import com.group10.API_ManageDormitory.entity.Role;
import com.group10.API_ManageDormitory.repository.RoleRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class SecurityRBACIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Setup initial user roles if needed for specific tests
        if (roleRepository.findByRoleName("TENANT").isEmpty()) {
            roleRepository.save(Role.builder().roleName("TENANT").description("Tenant").build());
        }
    }

    @Test
    @WithMockUser(username = "tenant_user", authorities = {"SCOPE_TENANT"})
    void tenantCannotDeleteInvoice_returnsForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/invoices/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tenant_user", authorities = {"SCOPE_TENANT"})
    void tenantCannotCreatePayment_returnsForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin_user", authorities = {"SCOPE_ADMIN"})
    void adminCanAccessRoomController_returnsOk() throws Exception {
        // We expect it might fail with 400 Bad Request due to missing request body or 404 because there's no data, 
        // but it should NOT return 403 Forbidden because ADMIN is authorized.
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/room-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // Assuming validation fails
    }

    // @Test
    // @WithMockUser(username = "tenant_user", authorities = {"SCOPE_TENANT"})
    // void tenantViewingOwnRoomMeter_requiresSetupFirst() throws Exception {
    //     // Real testing of MeterReadingService requires setting up Tenant, User, Room, etc.
    //     // For now, we expect 403 because no matching tenant/room exists in the mock DB for "tenant_user".
    //     mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/meter-readings?roomId=999"))
    //             .andExpect(MockMvcResultMatchers.status().isForbidden());
    // }
}
