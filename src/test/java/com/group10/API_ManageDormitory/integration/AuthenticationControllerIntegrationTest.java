package com.group10.API_ManageDormitory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group10.API_ManageDormitory.dtos.request.LoginRequest;
import com.group10.API_ManageDormitory.dtos.request.RegisterRequest;
import com.group10.API_ManageDormitory.entity.Role;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.repository.RoleRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthenticationControllerIntegrationTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mockMvc;

        private ObjectMapper objectMapper = new ObjectMapper();

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .build();

                Role role = roleRepository.findByRoleName("USER")
                                .orElseGet(() -> roleRepository.save(Role.builder()
                                                .roleName("USER")
                                                .description("Default User Role")
                                                .build()));

                User user = User.builder()
                                .username("testlogin")
                                .passwordHash(passwordEncoder.encode("password123"))
                                .fullName("Test Login User")
                                .email("testlogin@test.com")
                                .phoneNumber("0999999999")
                                .role(role)
                                .build();
                userRepository.save(user);
        }

        // --- POST /api/v1/auth/register ---

        @Test
        void register_HappyCase_ShouldCreateUser() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .username("newregister")
                                .password("password123")
                                .fullName("New Register User")
                                .email("register@test.com")
                                .phoneNumber("0111111111")
                                .build();

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result.username").value("newregister"));
        }

        @Test
        void register_NegativeCase_UsernameTooShort() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .username("ab") // < 3 chars
                                .password("password123")
                                .build();

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // --- POST /api/v1/auth/login ---

        @Test
        void login_HappyCase_ShouldReturnToken() throws Exception {
                LoginRequest request = LoginRequest.builder()
                                .username("testlogin")
                                .password("password123")
                                .build();

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result.authenticated").value(true))
                                .andExpect(jsonPath("$.result.token").exists());
        }

        @Test
        void login_NegativeCase_WrongPassword() throws Exception {
                LoginRequest request = LoginRequest.builder()
                                .username("testlogin")
                                .password("wrongpassword")
                                .build();

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized()); // Adjust based on your GlobalExceptionHandler
                                                                       // for
                                                                       // BadCredentialsException
        }

        // --- GET /api/v1/auth/me ---

        // Note: To test /me, we either need a valid token injected into the header or
        // @WithMockUser IF the security context binds it properly.
        // However, if the endpoint depends on extracting the user from the JWT token
        // string, @WithMockUser might not provide exactly what the logic expects if it
        // parses the raw token from the context.
        // Let's assume standard Spring Security context.

        @Test
        @WithMockUser(username = "testlogin")
        void getMyInfo_HappyCase_ShouldReturnUserInfo() throws Exception {
                mockMvc.perform(get("/api/v1/auth/me"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(1000))
                                .andExpect(jsonPath("$.result.username").value("testlogin"));
        }
}
