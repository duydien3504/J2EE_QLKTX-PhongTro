package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.month = :month AND i.year = :year AND i.paymentStatus = 'PAID' " +
            "AND (:buildingId IS NULL OR i.contract.room.floor.building.buildingId = :buildingId)")
    BigDecimal getTotalRevenueByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year, @Param("buildingId") Integer buildingId);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.month = :month AND i.year = :year AND i.paymentStatus = 'PAID' " +
            "AND i.contract.room.floor.building.manager.userId = :userId")
    BigDecimal getTotalRevenueByMonthAndYearForManager(@Param("month") Integer month, @Param("year") Integer year, @Param("userId") Integer userId);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.month = :month AND i.year = :year AND i.paymentStatus = 'PAID' " +
            "AND i.contract.room.floor.building.owner.userId = :userId")
    BigDecimal getTotalRevenueByMonthAndYearForOwner(@Param("month") Integer month, @Param("year") Integer year, @Param("userId") Integer userId);

    java.util.List<Invoice> findByContract_ContractId(Integer contractId);

    @Query("SELECT i FROM Invoice i WHERE " +
            "(:isAdmin = true OR " +
            "i.contract.room.floor.building.manager.username = :username OR " +
            "i.contract.room.floor.building.owner.username = :username)")
    Page<Invoice> findAllCustom(@Param("username") String username,
                                @Param("isAdmin") boolean isAdmin,
                                Pageable pageable);
}
