package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.response.*;
import com.group10.API_ManageDormitory.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/revenue")
    public ApiResponse<RevenueMonthResponse> getRevenue(
            @RequestParam Integer month,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer buildingId) {
        return ApiResponse.<RevenueMonthResponse>builder()
                .result(statisticsService.getRevenueByMonthAndYear(month, year, buildingId))
                .build();
    }

    @GetMapping("/rooms")
    public ApiResponse<RoomStatusStatisticsResponse> getRoomStatus(
            @RequestParam(required = false) Integer buildingId) {
        return ApiResponse.<RoomStatusStatisticsResponse>builder()
                .result(statisticsService.getRoomStatusStatistics(buildingId))
                .build();
    }

    @GetMapping("/revenue/detail")
    public ApiResponse<RevenueDetailResponse> getRevenueDetail(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ApiResponse.<RevenueDetailResponse>builder()
                .result(statisticsService.getRevenueDetailByMonthAndYear(month, year))
                .build();
    }

    @GetMapping("/expenses")
    public ApiResponse<List<ExpenseStatisticResponse>> getExpenseDistribution() {
        return ApiResponse.<List<ExpenseStatisticResponse>>builder()
                .result(statisticsService.getExpenseDistribution())
                .build();
    }

    @GetMapping("/occupancy/buildings")
    public ApiResponse<List<OccupancyByBuildingResponse>> getOccupancyByBuilding() {
        return ApiResponse.<List<OccupancyByBuildingResponse>>builder()
                .result(statisticsService.getOccupancyByBuilding())
                .build();
    }
}

