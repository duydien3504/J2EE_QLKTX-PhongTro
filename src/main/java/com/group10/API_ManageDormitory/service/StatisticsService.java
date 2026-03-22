package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.response.RevenueMonthResponse;
import com.group10.API_ManageDormitory.dtos.response.RoomStatusStatisticsResponse;
import com.group10.API_ManageDormitory.repository.InvoiceRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {
    private final InvoiceRepository invoiceRepository;
    private final RoomRepository roomRepository;

    public RevenueMonthResponse getRevenueByMonthAndYear(Integer month, Integer year) {
        BigDecimal total = invoiceRepository.getTotalRevenueByMonthAndYear(month, year);
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        return RevenueMonthResponse.builder()
                .month(month)
                .year(year)
                .totalRevenue(total)
                .build();
    }

    public RoomStatusStatisticsResponse getRoomStatusStatistics() {
        long totalRooms = roomRepository.count();
        long rentedRooms = roomRepository.countByCurrentStatus("RENTED"); // Or whatever your status string is
        long emptyRooms = totalRooms - rentedRooms; 

        return RoomStatusStatisticsResponse.builder()
                .totalRooms(totalRooms)
                .rentedRooms(rentedRooms)
                .emptyRooms(emptyRooms)
                .build();
    }
}
