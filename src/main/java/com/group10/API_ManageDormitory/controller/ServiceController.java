package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.BuildingServiceRequest;
import com.group10.API_ManageDormitory.dtos.request.ServiceInfoRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.BuildingServiceResponse;
import com.group10.API_ManageDormitory.dtos.response.ServiceInfoResponse;
import com.group10.API_ManageDormitory.service.ServiceConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceConfigService serviceConfigService;

    // Global Services
    @GetMapping("/services")
    public ApiResponse<List<ServiceInfoResponse>> getAllServices() {
        return ApiResponse.<List<ServiceInfoResponse>>builder()
                .result(serviceConfigService.getAllServices())
                .build();
    }

    @PostMapping("/services")
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public ApiResponse<ServiceInfoResponse> createService(@RequestBody @Valid ServiceInfoRequest request) {
        return ApiResponse.<ServiceInfoResponse>builder()
                .result(serviceConfigService.createService(request))
                .build();
    }

    @PutMapping("/services/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public ApiResponse<ServiceInfoResponse> updateService(@PathVariable Integer id,
            @RequestBody ServiceInfoRequest request) {
        return ApiResponse.<ServiceInfoResponse>builder()
                .result(serviceConfigService.updateService(id, request))
                .build();
    }

    // Building Services
    @GetMapping("/buildings/{id}/services")
    public ApiResponse<List<BuildingServiceResponse>> getBuildingServices(@PathVariable Integer id) {
        return ApiResponse.<List<BuildingServiceResponse>>builder()
                .result(serviceConfigService.getBuildingServices(id))
                .build();
    }

    @PostMapping("/buildings/{id}/services")
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public ApiResponse<BuildingServiceResponse> upsertBuildingService(
            @PathVariable Integer id, @RequestBody @Valid BuildingServiceRequest request) {
        return ApiResponse.<BuildingServiceResponse>builder()
                .result(serviceConfigService.upsertBuildingService(id, request))
                .build();
    }
}
