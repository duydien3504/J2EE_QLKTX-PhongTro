package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.UserCreationRequest;
import com.group10.API_ManageDormitory.dtos.response.PageResponse;
import com.group10.API_ManageDormitory.dtos.response.UserResponse;
import com.group10.API_ManageDormitory.entity.Role;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.mapper.UserMapper;
import com.group10.API_ManageDormitory.repository.RoleRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import com.group10.API_ManageDormitory.repository.BuildingRepository;
import com.group10.API_ManageDormitory.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BuildingRepository buildingRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<UserResponse> getUsers(String roleName, String searchTerm, int page, int size) {
        User requester = getRequester();
        String requesterRole = (requester.getRole() != null ? requester.getRole().getRoleName() : "").toUpperCase();
        boolean isOwner = requesterRole.equals("OWNER") || requesterRole.equals("SCOPE_OWNER");

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAllFiltered(roleName, isOwner, searchTerm, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(user -> {
                    java.util.List<String> bNames = buildingRepository.findByManager_UserId(user.getUserId())
                            .stream().map(com.group10.API_ManageDormitory.entity.Building::getBuildingName)
                            .collect(Collectors.toList());
                    return userMapper.toUserResponse(user, null, bNames);
                })
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .data(userResponses)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .build();
    }

    public UserResponse createUser(UserCreationRequest request) {
        validateRoleAccess(request.getRoleName());
        
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

        User saved = userRepository.save(user);
        java.util.List<String> bNames = buildingRepository.findByManager_UserId(saved.getUserId())
                .stream().map(com.group10.API_ManageDormitory.entity.Building::getBuildingName)
                .collect(Collectors.toList());
        return userMapper.toUserResponse(saved, null, bNames);
    }

    public UserResponse updateUserStatus(Integer id, boolean isActive) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        validateTargetUserAccess(user);
        
        user.setIsActive(isActive);
        User saved = userRepository.save(user);
        java.util.List<String> bNames = buildingRepository.findByManager_UserId(saved.getUserId())
                .stream().map(com.group10.API_ManageDormitory.entity.Building::getBuildingName)
                .collect(Collectors.toList());
        return userMapper.toUserResponse(saved, null, bNames);
    }

    public void deleteUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        validateTargetUserAccess(user);
        
        user.setIsDeleted(true);
        userRepository.save(user);
    }

    public UserResponse assignRole(Integer userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        validateTargetUserAccess(user);
        validateRoleAccess(roleName);
        
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRole(role);
        User saved = userRepository.save(user);
        java.util.List<String> bNames = buildingRepository.findByManager_UserId(saved.getUserId())
                .stream().map(com.group10.API_ManageDormitory.entity.Building::getBuildingName)
                .collect(Collectors.toList());
        return userMapper.toUserResponse(saved, null, bNames);
    }

    public UserResponse updateUser(Integer id, UserCreationRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        validateTargetUserAccess(user);
        validateRoleAccess(request.getRoleName());

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        if (request.getRoleName() != null) {
            Role role = roleRepository.findByRoleName(request.getRoleName())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            user.setRole(role);
        }

        User saved = userRepository.save(user);
        java.util.List<String> bNames = buildingRepository.findByManager_UserId(saved.getUserId())
                .stream().map(com.group10.API_ManageDormitory.entity.Building::getBuildingName)
                .collect(Collectors.toList());
        return userMapper.toUserResponse(saved, null, bNames);
    }

    private User getRequester() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private void validateTargetUserAccess(User targetUser) {
        User requester = getRequester();
        String requesterRole = (requester.getRole() != null ? requester.getRole().getRoleName() : "").toUpperCase();
        if (requesterRole.equals("ADMIN") || requesterRole.equals("SCOPE_ADMIN")) return;

        if (requesterRole.equals("OWNER") || requesterRole.equals("SCOPE_OWNER")) {
            String targetRole = (targetUser.getRole() != null ? targetUser.getRole().getRoleName() : "").toUpperCase();
            if (targetRole.equals("ADMIN") || targetRole.equals("OWNER") 
                || targetRole.equals("SCOPE_ADMIN") || targetRole.equals("SCOPE_OWNER")) {
                throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
            }
        }
    }

    private void validateRoleAccess(String targetRoleName) {
        if (targetRoleName == null) return;
        User requester = getRequester();
        String requesterRole = (requester.getRole() != null ? requester.getRole().getRoleName() : "").toUpperCase();
        if (requesterRole.equals("ADMIN") || requesterRole.equals("SCOPE_ADMIN")) return;

        if (requesterRole.equals("OWNER") || requesterRole.equals("SCOPE_OWNER")) {
            String target = targetRoleName.toUpperCase();
            if (target.equals("ADMIN") || target.equals("OWNER") 
                || target.equals("SCOPE_ADMIN") || target.equals("SCOPE_OWNER")) {
                throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
            }
        }
    }
}
