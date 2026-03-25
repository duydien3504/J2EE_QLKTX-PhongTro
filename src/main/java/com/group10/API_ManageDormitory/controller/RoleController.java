package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.entity.Role;
import com.group10.API_ManageDormitory.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<List<Role>> getAllRoles() {
        List<Role> allRoles = roleRepository.findAll();
        
        // Filter unique roles by name (case-insensitive)
        java.util.List<Role> distinctRoles = allRoles.stream()
                .collect(java.util.stream.Collectors.toMap(
                        r -> r.getRoleName().toUpperCase(),
                        r -> r,
                        (existing, replacement) -> existing
                ))
                .values().stream()
                .collect(java.util.stream.Collectors.toList());

        return ApiResponse.<List<Role>>builder()
                .result(distinctRoles)
                .build();
    }
}
