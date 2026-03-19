package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.BuildingRequest;
import com.group10.API_ManageDormitory.dtos.request.FloorRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.BuildingResponse;
import com.group10.API_ManageDormitory.dtos.response.FloorResponse;
import com.group10.API_ManageDormitory.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BuildingController {
    private final BuildingService buildingService;

    // Building Endpoints
    @GetMapping("/buildings")
    public ApiResponse<List<BuildingResponse>> getAllBuildings() {
        return ApiResponse.<List<BuildingResponse>>builder()
                .result(buildingService.getAllBuildings())
                .build();
    }

    @PostMapping("/buildings")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<BuildingResponse> createBuilding(@RequestBody @Valid BuildingRequest request) {
        return ApiResponse.<BuildingResponse>builder()
                .result(buildingService.createBuilding(request))
                .build();
    }

    @PutMapping("/buildings/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<BuildingResponse> updateBuilding(@PathVariable Integer id,
            @RequestBody BuildingRequest request) {
        return ApiResponse.<BuildingResponse>builder()
                .result(buildingService.updateBuilding(id, request))
                .build();
    }

    @DeleteMapping("/buildings/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<String> deleteBuilding(@PathVariable Integer id) {
        buildingService.deleteBuilding(id);
        return ApiResponse.<String>builder()
                .result("Building deleted successfully")
                .build();
    }

    // Floor Endpoints
    @GetMapping("/buildings/{id}/floors")
    public ApiResponse<List<FloorResponse>> getFloors(@PathVariable Integer id) {
        return ApiResponse.<List<FloorResponse>>builder()
                .result(buildingService.getFloorsByBuilding(id))
                .build();
    }

    @PostMapping("/floors")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<FloorResponse> createFloor(@RequestBody @Valid FloorRequest request) {
        return ApiResponse.<FloorResponse>builder()
                .result(buildingService.createFloor(request))
                .build();
    }
}
