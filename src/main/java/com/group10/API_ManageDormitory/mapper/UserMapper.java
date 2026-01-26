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
                .build();
    }
}
