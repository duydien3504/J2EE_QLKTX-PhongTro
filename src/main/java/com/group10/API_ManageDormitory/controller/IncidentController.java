package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.IncidentRequest;
import com.group10.API_ManageDormitory.dtos.request.IncidentStatusRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.IncidentResponse;
import com.group10.API_ManageDormitory.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {
    private final IncidentService incidentService;

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF') or hasAuthority('SCOPE_TENANT')")
    public ApiResponse<IncidentResponse> createIncident(@RequestBody @Valid IncidentRequest request) {
        return ApiResponse.<IncidentResponse>builder()
                .result(incidentService.createIncident(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<List<IncidentResponse>> getAllIncidents() {
        return ApiResponse.<List<IncidentResponse>>builder()
                .result(incidentService.getAllIncidents())
                .build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<IncidentResponse> updateIncidentStatus(
            @PathVariable Integer id,
            @RequestBody @Valid IncidentStatusRequest request) {
        return ApiResponse.<IncidentResponse>builder()
                .result(incidentService.updateIncidentStatus(id, request.getStatus()))
                .build();
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<List<IncidentResponse>> getIncidentsByRoom(@PathVariable Integer roomId) {
        return ApiResponse.<List<IncidentResponse>>builder()
                .result(incidentService.getIncidentsByRoom(roomId))
                .build();
    }

    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF') or hasAuthority('SCOPE_TENANT')")
    public ApiResponse<List<IncidentResponse>> getIncidentsByTenant(@PathVariable Integer tenantId) {
        return ApiResponse.<List<IncidentResponse>>builder()
                .result(incidentService.getIncidentsByTenant(tenantId))
                .build();
    }
}
