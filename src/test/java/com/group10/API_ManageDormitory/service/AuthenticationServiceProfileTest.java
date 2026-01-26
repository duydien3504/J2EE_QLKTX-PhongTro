package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.ChangePasswordRequest;
import com.group10.API_ManageDormitory.dtos.request.UpdateProfileRequest;
import com.group10.API_ManageDormitory.dtos.response.UserResponse;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.mapper.UserMapper;
import com.group10.API_ManageDormitory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceProfileTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void changePassword_success() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("oldPass")
                .newPassword("newPass")
                .build();
        User user = User.builder().username("user").passwordHash("encodedOldPass").build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        authenticationService.changePassword(request);

        assertEquals("encodedNewPass", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void getMyInfo_success() {
        User user = User.builder().username("user").build();
        UserResponse response = UserResponse.builder().username("user").build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(response);

        var result = authenticationService.getMyInfo();

        assertNotNull(result);
        assertEquals("user", result.getUsername());
    }
}
