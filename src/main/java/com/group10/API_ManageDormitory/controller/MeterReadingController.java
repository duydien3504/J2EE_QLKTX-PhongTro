package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.MeterReadingRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.MeterReadingResponse;
import com.group10.API_ManageDormitory.service.MeterReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meter-readings")
@RequiredArgsConstructor
public class MeterReadingController {
    private final MeterReadingService meterReadingService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF') or hasAuthority('SCOPE_TENANT')")
    public ApiResponse<List<MeterReadingResponse>> getReadings(
            @RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ApiResponse.<List<MeterReadingResponse>>builder()
                .result(meterReadingService.getReadings(roomId, month, year))
                .build();
    }

    @GetMapping("/last-month")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<MeterReadingResponse> getLastMonthReading(
            @RequestParam Integer roomId, @RequestParam Integer serviceId) {
        return ApiResponse.<MeterReadingResponse>builder()
                .result(meterReadingService.getLastMonthReading(roomId, serviceId))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<MeterReadingResponse> recordReading(@RequestBody @Valid MeterReadingRequest request) {
        return ApiResponse.<MeterReadingResponse>builder()
                .result(meterReadingService.recordReading(request))
                .build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public ApiResponse<List<MeterReadingResponse>> bulkRecord(@RequestBody @Valid List<MeterReadingRequest> requests) {
        return ApiResponse.<List<MeterReadingResponse>>builder()
                .result(meterReadingService.bulkRecord(requests))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public ApiResponse<MeterReadingResponse> updateReading(@PathVariable Integer id,
            @RequestBody MeterReadingRequest request) {
        return ApiResponse.<MeterReadingResponse>builder()
                .result(meterReadingService.updateReading(id, request))
                .build();
    }
}
