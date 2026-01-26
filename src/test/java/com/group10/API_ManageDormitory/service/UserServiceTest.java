package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.UserCreationRequest;
import com.group10.API_ManageDormitory.dtos.response.UserResponse;
import com.group10.API_ManageDormitory.entity.Role;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.mapper.UserMapper;
import com.group10.API_ManageDormitory.repository.RoleRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("staff")
                .password("password")
                .roleName("STAFF")
                .build();
        Role role = Role.builder().roleName("STAFF").build();
        User user = User.builder().username("staff").role(role).build();
        UserResponse response = UserResponse.builder().username("staff").build();

        when(userRepository.findByUsername("staff")).thenReturn(Optional.empty());
        when(userMapper.toUserFromCreationRequest(request)).thenReturn(user);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(roleRepository.findByRoleName("STAFF")).thenReturn(Optional.of(role));
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(response);

        var result = userService.createUser(request);

        assertEquals("staff", result.getUsername());
        verify(userRepository).save(any());
    }

    @Test
    void deleteUser_success() {
        User user = User.builder().userId(1).isDeleted(false).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.deleteUser(1);

        assertTrue(user.getIsDeleted());
        verify(userRepository).save(user); // Soft delete calls save
    }
}
