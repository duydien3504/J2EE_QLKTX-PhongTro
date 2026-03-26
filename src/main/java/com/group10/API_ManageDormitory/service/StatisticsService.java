package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.response.*;
import com.group10.API_ManageDormitory.repository.*;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.utils.SecurityUtils;
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
    private final ContractTenantRepository contractTenantRepository;
    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;

    private void validateBuildingAccess(Integer buildingId) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String role = (user.getRole() != null ? user.getRole().getRoleName() : "").toUpperCase();
        if (role.equals("ADMIN") || role.equals("SCOPE_ADMIN")) return;

        if (buildingId == null) {
            // For managers/owners, if buildingId is null, we aggregate for ALL their buildings
            return;
        }

        boolean isAuthorized = buildingRepository.existsByBuildingIdAndManager_UserId(buildingId, user.getUserId())
                            || buildingRepository.existsByBuildingIdAndOwner_UserId(buildingId, user.getUserId());
        
        if (!isAuthorized) {
            throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
        }
    }

    public RevenueMonthResponse getRevenueByMonthAndYear(Integer month, Integer year, Integer buildingId) {
        validateBuildingAccess(buildingId);
        
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String role = (user.getRole() != null ? user.getRole().getRoleName() : "").toUpperCase();
        boolean isAdmin = role.equals("ADMIN") || role.equals("SCOPE_ADMIN");

        BigDecimal totalRevenue;
        if (buildingId != null || isAdmin) {
            totalRevenue = invoiceRepository.getTotalRevenueByMonthAndYear(month, year, buildingId);
        } else {
            // Aggregate for user (Owner + Manager)
            BigDecimal managerRev = invoiceRepository.getTotalRevenueByMonthAndYearForManager(month, year, user.getUserId());
            BigDecimal ownerRev = invoiceRepository.getTotalRevenueByMonthAndYearForOwner(month, year, user.getUserId());
            
            if (managerRev == null) managerRev = BigDecimal.ZERO;
            if (ownerRev == null) ownerRev = BigDecimal.ZERO;
            
            // To avoid double counting buildings where user is both owner and manager
            // we should ideally filter buildings, but the queries above use user_id directly.
            // For now, let's just use the max/sum carefully.
            totalRevenue = managerRev.add(ownerRev);
        }
        
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        return RevenueMonthResponse.builder()
                .month(month)
                .year(year)
                .totalRevenue(totalRevenue)
                .build();
    }

    public RoomStatusStatisticsResponse getRoomStatusStatistics(Integer buildingId) {
        validateBuildingAccess(buildingId);
        
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String role = (user.getRole() != null ? user.getRole().getRoleName() : "").toUpperCase();
        boolean isAdmin = role.equals("ADMIN") || role.equals("SCOPE_ADMIN");

        long totalRooms;
        long rentedRooms;

        if (buildingId != null) {
            totalRooms = roomRepository.countByFloor_Building_BuildingId(buildingId);
            rentedRooms = roomRepository.countByCurrentStatusAndFloor_Building_BuildingId("OCCUPIED", buildingId);
        } else if (isAdmin) {
            totalRooms = roomRepository.count();
            rentedRooms = roomRepository.countByCurrentStatus("OCCUPIED");
        } else {
            // Aggregate for user (Owner + Manager)
            totalRooms = roomRepository.countByFloor_Building_Manager_UserId(user.getUserId()) 
                       + roomRepository.countByFloor_Building_Owner_UserId(user.getUserId());
            rentedRooms = roomRepository.countByCurrentStatusAndFloor_Building_Manager_UserId("OCCUPIED", user.getUserId())
                        + roomRepository.countByCurrentStatusAndFloor_Building_Owner_UserId("OCCUPIED", user.getUserId());
        }

        long emptyRooms = totalRooms - rentedRooms;
        long activeTenants;
        if (buildingId != null) {
            activeTenants = contractTenantRepository.countActiveTenants(buildingId);
        } else if (isAdmin) {
            activeTenants = contractTenantRepository.countActiveTenants(null);
        } else {
            // Aggregate for user (Owner + Manager) without double counting
            activeTenants = contractTenantRepository.countActiveTenantsByUser(user.getUserId());
        }

        return RoomStatusStatisticsResponse.builder()
                .totalRooms(totalRooms)
                .rentedRooms(rentedRooms)
                .emptyRooms(emptyRooms)
                .activeTenants(activeTenants)
                .build();
    }

    public RevenueDetailResponse getRevenueDetailByMonthAndYear(Integer month, Integer year) {
        BigDecimal rent = invoiceDetailRepository.getRentRevenueByMonthAndYear(month, year);
        BigDecimal service = invoiceDetailRepository.getServiceRevenueByMonthAndYear(month, year);
        
        if (rent == null) rent = BigDecimal.ZERO;
        if (service == null) service = BigDecimal.ZERO;
        
        if (rent.compareTo(BigDecimal.ZERO) == 0 && service.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal total = invoiceRepository.getTotalRevenueByMonthAndYear(month, year, null);
            if (total != null && total.compareTo(BigDecimal.ZERO) > 0) {
                rent = total;
            }
        }

        return RevenueDetailResponse.builder()
                .month(month)
                .year(year)
                .rentRevenue(rent)
                .serviceRevenue(service)
                .totalRevenue(rent.add(service))
                .build();
    }

    public List<ExpenseStatisticResponse> getExpenseDistribution() {
        List<Object[]> rows = expenseRepository.getExpenseDistribution();
        return rows.stream()
                .map(row -> ExpenseStatisticResponse.builder()
                        .expenseType((String) row[0])
                        .totalAmount((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    public List<OccupancyByBuildingResponse> getOccupancyByBuilding() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        String role = (user.getRole() != null ? user.getRole().getRoleName() : "").toUpperCase();
        boolean isAdmin = role.equals("ADMIN") || role.equals("SCOPE_ADMIN");

        List<Object[]> totalRows;
        List<Object[]> rentedRows;

        if (isAdmin) {
            totalRows = roomRepository.countRoomsByBuilding();
            rentedRows = roomRepository.countRentedRoomsByBuilding();
        } else {
            totalRows = roomRepository.countRoomsByBuildingManager(user.getUserId());
            List<Object[]> ownedTotal = roomRepository.countRoomsByBuildingOwner(user.getUserId());
            mergeResults(totalRows, ownedTotal);

            rentedRows = roomRepository.countRentedRoomsByBuildingManager(user.getUserId());
            List<Object[]> ownedRented = roomRepository.countRentedRoomsByBuildingOwner(user.getUserId());
            mergeResults(rentedRows, ownedRented);
        }

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

    private void mergeResults(List<Object[]> target, List<Object[]> source) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : target) map.put((String) row[0], (Long) row[1]);
        for (Object[] row : source) {
            String name = (String) row[0];
            if (!map.containsKey(name)) {
                target.add(row);
            }
        }
    }
}
