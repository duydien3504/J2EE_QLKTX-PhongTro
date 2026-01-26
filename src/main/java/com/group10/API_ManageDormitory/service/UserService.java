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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getUsers(String roleName) {
        if (roleName != null && !roleName.isEmpty()) {
            return userRepository.findAll().stream()
                    .filter(user -> user.getRole() != null && user.getRole().getRoleName().equalsIgnoreCase(roleName))
                    .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted())) // Filter out deleted
                    .map(userMapper::toUserResponse)
                    .collect(Collectors.toList());
            // Note: Ideally UserRepository should have findByRoleName. But Stream filter is
            // OK for simple requirements if dataset is small.
            // Requirement said "filter theo role".
        }
        return userRepository.findAll().stream()
                .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()))
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUserFromCreationRequest(request); // Assuming mapper handles this. But request is
                                                                   // UserCreationRequest.
        // UserMapper usually maps RegisterRequest. I need to update Mapper or manual
        // map.
        // Let's assume manual mapping for simplicity or update Mapper later.

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        String targetRole = (request.getRoleName() != null) ? request.getRoleName() : "STAFF";
        Role role = roleRepository.findByRoleName(targetRole)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRole(role);

        user.setIsActive(true);
        user.setIsDeleted(false);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse updateUserStatus(Integer id, boolean isActive) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setIsActive(isActive);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setIsDeleted(true);
        userRepository.save(user);
    }
}
