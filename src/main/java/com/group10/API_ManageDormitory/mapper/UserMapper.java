package com.group10.API_ManageDormitory.mapper;

import com.group10.API_ManageDormitory.dtos.request.RegisterRequest;
import com.group10.API_ManageDormitory.dtos.response.UserResponse;
import com.group10.API_ManageDormitory.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toUser(RegisterRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .username(request.getUsername())
                .passwordHash(request.getPassword()) // Will be encoded in service
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    public User toUserFromCreationRequest(com.group10.API_ManageDormitory.dtos.request.UserCreationRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .username(request.getUsername())
                .passwordHash(request.getPassword()) // Will be encoded in service
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return toUserResponse(user, null, java.util.Collections.emptyList());
    }

    public UserResponse toUserResponse(User user, Integer tenantId) {
        return toUserResponse(user, tenantId, java.util.Collections.emptyList());
    }

    public UserResponse toUserResponse(User user, Integer tenantId, java.util.List<String> managedBuildings) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : null)
                .tenantId(tenantId)
                .managedBuildings(managedBuildings)
                .build();
    }
}
