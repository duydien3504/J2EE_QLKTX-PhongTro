package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.UserCreationRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.UserResponse;
import com.group10.API_ManageDormitory.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<List<UserResponse>> getUsers(@RequestParam(required = false) String role) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers(role))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<UserResponse> updateUserStatus(@PathVariable Integer id, @RequestParam boolean isActive) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserStatus(id, isActive))
                .build();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<UserResponse> assignRole(@PathVariable Integer id, @RequestBody java.util.Map<String, String> body) {
        String roleName = body.get("roleName");
        return ApiResponse.<UserResponse>builder()
                .result(userService.assignRole(id, roleName))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<UserResponse> updateUser(@PathVariable Integer id, @RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<String> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ApiResponse.<String>builder()
                .result("User has been deleted")
                .build();
    }
}
