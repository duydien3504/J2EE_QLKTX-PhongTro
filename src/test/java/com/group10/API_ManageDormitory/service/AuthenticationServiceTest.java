package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.LoginRequest;
import com.group10.API_ManageDormitory.dtos.request.RegisterRequest;
import com.group10.API_ManageDormitory.dtos.response.AuthenticationResponse;
import com.group10.API_ManageDormitory.dtos.response.UserResponse;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.mapper.UserMapper;
import com.group10.API_ManageDormitory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private UserMapper userMapper;

        @Mock
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private AuthenticationService authenticationService;

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY",
                                "546546456546345345435345345345345345435345634645645612345678901234567890");
        }

        @Test
        void register_validRequest_success() {
                // GIVEN
                RegisterRequest request = RegisterRequest.builder()
                                .username("testuser")
                                .password("password123")
                                .fullName("Test User")
                                .email("test@example.com")
                                .build();

                User user = User.builder()
                                .username("testuser")
                                .build();

                UserResponse userResponse = UserResponse.builder()
                                .username("testuser")
                                .fullName("Test User")
                                .build();

                when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
                when(userMapper.toUser(any())).thenReturn(user);
                when(passwordEncoder.encode(any())).thenReturn("encoded_password");
                when(userRepository.save(any())).thenReturn(user);
                when(userMapper.toUserResponse(any())).thenReturn(userResponse);

                // WHEN
                var result = authenticationService.register(request);

                // THEN
                assertNotNull(result);
                assertEquals("testuser", result.getUsername());
        }

        @Test
        void register_userExisted_fail() {
                // GIVEN
                RegisterRequest request = RegisterRequest.builder()
                                .username("testuser")
                                .build();

                when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

                // WHEN
                var exception = assertThrows(AppException.class, () -> authenticationService.register(request));

                // THEN
                assertEquals(ErrorCode.USER_EXISTED, exception.getErrorCode());
        }

        @Test
        void login_validRequest_success() {
                // GIVEN
                LoginRequest request = LoginRequest.builder()
                                .username("testuser")
                                .password("password123")
                                .build();

                User user = User.builder()
                                .username("testuser")
                                .passwordHash("encoded_password")
                                .userId(1)
                                .build();

                when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
                when(passwordEncoder.matches(any(), any())).thenReturn(true);

                // WHEN
                var result = authenticationService.login(request);

                // THEN
                assertNotNull(result);
                assertTrue(result.isAuthenticated());
                assertNotNull(result.getToken());
        }

        @Test
        void login_userNotFound_fail() {
                LoginRequest request = LoginRequest.builder().username("nouser").password("pwd").build();
                when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

                var exception = assertThrows(AppException.class, () -> authenticationService.login(request));
                assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
        }
}
