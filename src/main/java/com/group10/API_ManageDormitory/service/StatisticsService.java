package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.response.*;
import com.group10.API_ManageDormitory.repository.ExpenseRepository;
import com.group10.API_ManageDormitory.repository.InvoiceDetailRepository;
import com.group10.API_ManageDormitory.repository.InvoiceRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final ExpenseRepository expenseRepository;
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
        long rentedRooms = roomRepository.countByCurrentStatus("RENTED");
        long emptyRooms = totalRooms - rentedRooms;

        return RoomStatusStatisticsResponse.builder()
                .totalRooms(totalRooms)
                .rentedRooms(rentedRooms)
                .emptyRooms(emptyRooms)
                .build();
    }

    /**
     * Chi tiết doanh thu theo tháng: Tiền phòng vs Dịch vụ
     */
    public RevenueDetailResponse getRevenueDetailByMonthAndYear(Integer month, Integer year) {
        BigDecimal rent = invoiceDetailRepository.getRentRevenueByMonthAndYear(month, year);
        BigDecimal service = invoiceDetailRepository.getServiceRevenueByMonthAndYear(month, year);

        return RevenueDetailResponse.builder()
                .month(month)
                .year(year)
                .rentRevenue(rent)
                .serviceRevenue(service)
                .totalRevenue(rent.add(service))
                .build();
    }

    /**
     * Cơ cấu chi phí: nhóm theo loại chi phí
     */
    public List<ExpenseStatisticResponse> getExpenseDistribution() {
        List<Object[]> rows = expenseRepository.getExpenseDistribution();
        return rows.stream()
                .map(row -> ExpenseStatisticResponse.builder()
                        .expenseType((String) row[0])
                        .totalAmount((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Tỷ lệ lấp đầy theo từng cơ sở (Building)
     */
    public List<OccupancyByBuildingResponse> getOccupancyByBuilding() {
        List<Object[]> totalRows = roomRepository.countRoomsByBuilding();
        List<Object[]> rentedRows = roomRepository.countRentedRoomsByBuilding();

        // Map buildingName -> rented count
        Map<String, Long> rentedMap = new HashMap<>();
        for (Object[] row : rentedRows) {
            rentedMap.put((String) row[0], (Long) row[1]);
        }

        return totalRows.stream()
                .map(row -> {
                    String name = (String) row[0];
                    long total = (Long) row[1];
                    long rented = rentedMap.getOrDefault(name, 0L);
                    return OccupancyByBuildingResponse.builder()
                            .buildingName(name)
                            .totalRooms(total)
                            .occupiedRooms(rented)
                            .vacantRooms(total - rented)
                            .build();
                })
                .collect(Collectors.toList());
    }
}

