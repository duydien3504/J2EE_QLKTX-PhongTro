package com.group10.API_ManageDormitory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group10.API_ManageDormitory.dtos.request.RegisterRequest;
import com.group10.API_ManageDormitory.dtos.request.ChangePasswordRequest;
import com.group10.API_ManageDormitory.dtos.request.UpdateProfileRequest;
import com.group10.API_ManageDormitory.dtos.response.UserResponse;
import com.group10.API_ManageDormitory.service.AuthenticationService;
import com.group10.API_ManageDormitory.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

        private MockMvc mockMvc;

        @Mock
        private AuthenticationService authenticationService;

        @InjectMocks
        private AuthenticationController authenticationController;

        private ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setup() {
                mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @Test
        void register_validRequest_success() throws Exception {
                // GIVEN
                RegisterRequest request = RegisterRequest.builder()
                                .username("testuser")
                                .password("password123")
                                .fullName("Test User")
                                .email("test@example.com")
                                .build();

                UserResponse userResponse = UserResponse.builder()
                                .userId(1)
                                .username("testuser")
                                .fullName("Test User")
                                .email("test@example.com")
                                .build();

                Mockito.when(authenticationService.register(Mockito.any())).thenReturn(userResponse);

                // WHEN, THEN
                mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000))
                                .andExpect(MockMvcResultMatchers.jsonPath("result.username").value("testuser"));
        }

        @Test
        void register_invalidUsername_fail() throws Exception {
                // GIVEN
                RegisterRequest request = RegisterRequest.builder()
                                .username("us") // Invalid < 3 chars
                                .password("password123")
                                .build();

                // WHEN, THEN
                mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1003)); // USERNAME_INVALID
        }

        @Test
        void changePassword_success() throws Exception {
                ChangePasswordRequest request = ChangePasswordRequest.builder()
                                .currentPassword("oldPass")
                                .newPassword("newPass")
                                .build();

                // No return value, just void call in service

                mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/change-password")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("result")
                                                .value("Password changed successfully"));
        }

        @Test
        void getMyInfo_success() throws Exception {
                UserResponse response = UserResponse.builder().username("user").build();

                Mockito.when(authenticationService.getMyInfo()).thenReturn(response);

                mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth/me"))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("result.username").value("user"));
        }

        @Test
        void updateProfile_success() throws Exception {
                UpdateProfileRequest request = UpdateProfileRequest.builder().fullName("New Name").build();
                UserResponse response = UserResponse.builder().username("user").fullName("New Name").build();

                Mockito.when(authenticationService.updateProfile(Mockito.any())).thenReturn(response);

                mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/auth/me")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("result.fullName").value("New Name"));
        }
}
