package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.RevenueMonthResponse;
import com.group10.API_ManageDormitory.dtos.response.RoomStatusStatisticsResponse;
import com.group10.API_ManageDormitory.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/revenue")
    public ApiResponse<RevenueMonthResponse> getRevenue(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ApiResponse.<RevenueMonthResponse>builder()
                .result(statisticsService.getRevenueByMonthAndYear(month, year))
                .build();
    }

    @GetMapping("/rooms")
    public ApiResponse<RoomStatusStatisticsResponse> getRoomStatus() {
        return ApiResponse.<RoomStatusStatisticsResponse>builder()
                .result(statisticsService.getRoomStatusStatistics())
                .build();
    }
}
