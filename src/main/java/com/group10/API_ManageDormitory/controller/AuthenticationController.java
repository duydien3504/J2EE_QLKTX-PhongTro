package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.ChangePasswordRequest;
import com.group10.API_ManageDormitory.dtos.request.LoginRequest;
import com.group10.API_ManageDormitory.dtos.request.RegisterRequest;
import com.group10.API_ManageDormitory.dtos.request.UpdateProfileRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.AuthenticationResponse;
import com.group10.API_ManageDormitory.dtos.response.UserResponse;
import com.group10.API_ManageDormitory.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(authenticationService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        var result = authenticationService.login(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PutMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ApiResponse.<String>builder()
                .result("Password changed successfully")
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(authenticationService.getMyInfo())
                .build();
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(authenticationService.updateProfile(request))
                .build();
    }
}
