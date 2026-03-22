package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.RoomRequest;
import com.group10.API_ManageDormitory.dtos.request.RoomTypeRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.RoomResponse;
import com.group10.API_ManageDormitory.dtos.response.RoomTypeResponse;
import com.group10.API_ManageDormitory.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    // RoomType Endpoints
    @GetMapping("/room-types")
    public ApiResponse<List<RoomTypeResponse>> getAllRoomTypes() {
        return ApiResponse.<List<RoomTypeResponse>>builder()
                .result(roomService.getAllRoomTypes())
                .build();
    }

    @PostMapping("/room-types")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<RoomTypeResponse> createRoomType(@RequestBody @Valid RoomTypeRequest request) {
        return ApiResponse.<RoomTypeResponse>builder()
                .result(roomService.createRoomType(request))
                .build();
    }

    // Room Endpoints
    @GetMapping("/rooms")
    public ApiResponse<List<RoomResponse>> getRooms(
            @RequestParam(required = false) Integer floorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ApiResponse.<List<RoomResponse>>builder()
                .result(roomService.getRooms(floorId, status, minPrice, maxPrice))
                .build();
    }

    @GetMapping("/rooms/{id}")
    public ApiResponse<RoomResponse> getRoom(@PathVariable Integer id) {
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.getRoomDetail(id))
                .build();
    }

    @PostMapping("/rooms")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<RoomResponse> createRoom(@RequestBody @Valid RoomRequest request) {
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.createRoom(request))
                .build();
    }

    @PutMapping("/rooms/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<RoomResponse> updateRoom(@PathVariable Integer id, @RequestBody RoomRequest request) {
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.updateRoom(id, request))
                .build();
    }

    @PatchMapping("/rooms/{id}/status")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<RoomResponse> updateRoomStatus(@PathVariable Integer id,
            @RequestBody Map<String, String> statusMap) {
        // Expecting {"status": "NEW_STATUS"}
        String status = statusMap.get("status");
        if (status == null) {
            throw new RuntimeException("Status is required");
        }
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.updateRoomStatus(id, status))
                .build();
    }

    @PostMapping(value = "/rooms/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<RoomResponse> uploadRoomImages(
            @PathVariable Integer id,
            @RequestParam("images") List<MultipartFile> images) throws IOException {
        return ApiResponse.<RoomResponse>builder()
                .result(roomService.uploadRoomImages(id, images))
                .build();
    }
}

