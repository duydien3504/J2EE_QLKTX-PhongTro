package com.group10.API_ManageDormitory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group10.API_ManageDormitory.dtos.request.UserCreationRequest;
import com.group10.API_ManageDormitory.entity.Role;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.repository.RoleRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserControllerIntegrationTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mockMvc;

        private ObjectMapper objectMapper = new ObjectMapper();

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        private Role savedRole;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .build();

                // Prepare initial data
                savedRole = roleRepository.findByRoleName("ADMIN")
                                .orElseGet(() -> roleRepository.save(Role.builder()
                                                .roleName("ADMIN")
                                                .description("Admin role")
                                                .build()));

                User user = User.builder()
                                .username("existinguser")
                                .passwordHash("hashedpassword")
                                .fullName("Existing User")
                                .email("existing@test.com")
                                .phoneNumber("0123456789")
                                .role(savedRole)
                                .build();
                userRepository.save(user);
        }

        // --- GET /api/v1/users ---

        @Test
        @WithMockUser(authorities = "SCOPE_ADMIN")
        void getUsers_HappyCase_ShouldReturnUsersList() throws Exception {
                mockMvc.perform(get("/api/v1/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result", hasSize(userRepository.findAll().size())));
        }

        @Test
        @WithMockUser(authorities = "SCOPE_USER")
        void getUsers_NegativeCase_UnauthorizedRole() throws Exception {
                mockMvc.perform(get("/api/v1/users"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getUsers_NegativeCase_Unauthenticated() throws Exception {
                mockMvc.perform(get("/api/v1/users"))
                                .andExpect(status().isUnauthorized());
        }

        // --- POST /api/v1/users ---

        @Test
        @WithMockUser(authorities = "SCOPE_ADMIN")
        void createUser_HappyCase_ShouldCreateUser() throws Exception {
                UserCreationRequest request = UserCreationRequest.builder()
                                .username("newuser")
                                .password("password123")
                                .fullName("New User")
                                .email("newuser@test.com")
                                .phoneNumber("0987654321")
                                .roleName("ADMIN")
                                .build();

                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result.username").value("newuser"))
                                .andExpect(jsonPath("$.result.email").value("newuser@test.com"));
        }

        @Test
        @WithMockUser(authorities = "SCOPE_ADMIN")
        void createUser_NegativeCase_MissingUsername() throws Exception {
                UserCreationRequest request = UserCreationRequest.builder()
                                .password("password123")
                                .build();

                mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // --- PUT /api/v1/users/{id}/status ---

        @Test
        @WithMockUser(authorities = "SCOPE_ADMIN")
        void updateUserStatus_HappyCase_ShouldUpdateStatus() throws Exception {
                User user = userRepository.findByUsername("existinguser").orElseThrow();

                mockMvc.perform(put("/api/v1/users/" + user.getUserId() + "/status")
                                .param("isActive", "false"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result.isActive").value(false));
        }

        @Test
        @WithMockUser(authorities = "SCOPE_ADMIN")
        void updateUserStatus_NegativeCase_UserNotFound() throws Exception {
                mockMvc.perform(put("/api/v1/users/999/status")
                                .param("isActive", "false"))
                                .andExpect(status().isNotFound()); // Or whatever status your exception handler returns
        }

        // --- DELETE /api/v1/users/{id} ---

        @Test
        @WithMockUser(authorities = "SCOPE_ADMIN")
        void deleteUser_HappyCase_ShouldDeleteUser() throws Exception {
                User user = userRepository.findByUsername("existinguser").orElseThrow();

                mockMvc.perform(delete("/api/v1/users/" + user.getUserId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result").value("User has been deleted"));
        }
}
