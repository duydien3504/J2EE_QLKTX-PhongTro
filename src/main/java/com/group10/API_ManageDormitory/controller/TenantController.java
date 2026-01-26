package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.TenantRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.TenantResponse;
import com.group10.API_ManageDormitory.service.TenantService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<List<TenantResponse>> getTenants(@RequestParam(required = false) String keyword) {
        return ApiResponse.<List<TenantResponse>>builder()
                .result(tenantService.getTenants(keyword))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<TenantResponse> getTenant(@PathVariable Integer id) {
        return ApiResponse.<TenantResponse>builder()
                .result(tenantService.getTenant(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<TenantResponse> createTenant(@RequestBody @Valid TenantRequest request) {
        return ApiResponse.<TenantResponse>builder()
                .result(tenantService.createTenant(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<TenantResponse> updateTenant(@PathVariable Integer id, @RequestBody TenantRequest request) {
        return ApiResponse.<TenantResponse>builder()
                .result(tenantService.updateTenant(id, request))
                .build();
    }

    @PostMapping(value = "/{id}/upload-cccd", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<TenantResponse> uploadCCCD(
            @PathVariable Integer id,
            @RequestParam(required = false) MultipartFile frontImage,
            @RequestParam(required = false) MultipartFile backImage) throws IOException {
        return ApiResponse.<TenantResponse>builder()
                .result(tenantService.uploadCCCD(id, frontImage, backImage))
                .build();
    }

    @GetMapping("/export-police")
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public void exportPolice(HttpServletResponse response) throws IOException {
        tenantService.exportTenantsToExcel(response);
    }
}
