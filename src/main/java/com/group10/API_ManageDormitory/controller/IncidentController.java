package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.IncidentRequest;
import com.group10.API_ManageDormitory.dtos.request.IncidentStatusRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.IncidentResponse;
import com.group10.API_ManageDormitory.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {
    private final IncidentService incidentService;

    @PostMapping
    public ApiResponse<IncidentResponse> createIncident(@RequestBody @Valid IncidentRequest request) {
        return ApiResponse.<IncidentResponse>builder()
                .result(incidentService.createIncident(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<IncidentResponse>> getAllIncidents() {
        return ApiResponse.<List<IncidentResponse>>builder()
                .result(incidentService.getAllIncidents())
                .build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<IncidentResponse> updateIncidentStatus(
            @PathVariable Integer id,
            @RequestBody @Valid IncidentStatusRequest request) {
        return ApiResponse.<IncidentResponse>builder()
                .result(incidentService.updateIncidentStatus(id, request.getStatus()))
                .build();
    }

    @GetMapping("/room/{roomId}")
    public ApiResponse<List<IncidentResponse>> getIncidentsByRoom(@PathVariable Integer roomId) {
        return ApiResponse.<List<IncidentResponse>>builder()
                .result(incidentService.getIncidentsByRoom(roomId))
                .build();
    }

    @GetMapping("/tenant/{tenantId}")
    public ApiResponse<List<IncidentResponse>> getIncidentsByTenant(@PathVariable Integer tenantId) {
        return ApiResponse.<List<IncidentResponse>>builder()
                .result(incidentService.getIncidentsByTenant(tenantId))
                .build();
    }
}
