package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.AssetRequest;
import com.group10.API_ManageDormitory.dtos.request.RoomAssetRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.AssetResponse;
import com.group10.API_ManageDormitory.dtos.response.RoomAssetResponse;
import com.group10.API_ManageDormitory.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    // Assets
    @GetMapping("/assets")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<List<AssetResponse>> getAllAssets() {
        return ApiResponse.<List<AssetResponse>>builder()
                .result(assetService.getAllAssets())
                .build();
    }

    @PostMapping("/assets")
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public ApiResponse<AssetResponse> createAsset(@RequestBody @Valid AssetRequest request) {
        return ApiResponse.<AssetResponse>builder()
                .result(assetService.createAsset(request))
                .build();
    }

    // Room Assets
    @GetMapping("/rooms/{id}/assets")
    public ApiResponse<List<RoomAssetResponse>> getAssetsByRoom(@PathVariable Integer id) {
        return ApiResponse.<List<RoomAssetResponse>>builder()
                .result(assetService.getAssetsByRoom(id))
                .build();
    }

    @PostMapping("/room-assets")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<RoomAssetResponse> assignAssetToRoom(@RequestBody @Valid RoomAssetRequest request) {
        return ApiResponse.<RoomAssetResponse>builder()
                .result(assetService.assignAssetToRoom(request))
                .build();
    }

    @PutMapping("/room-assets/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<RoomAssetResponse> updateRoomAsset(@PathVariable Integer id,
            @RequestBody RoomAssetRequest request) {
        return ApiResponse.<RoomAssetResponse>builder()
                .result(assetService.updateRoomAsset(id, request))
                .build();
    }

    @DeleteMapping("/room-assets/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<String> removeRoomAsset(@PathVariable Integer id) {
        assetService.removeRoomAsset(id);
        return ApiResponse.<String>builder()
                .result("Asset removed from room successfully")
                .build();
    }
}
