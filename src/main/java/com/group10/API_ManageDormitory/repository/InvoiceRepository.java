package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.month = :month AND i.year = :year AND i.paymentStatus = 'PAID'")
    BigDecimal getTotalRevenueByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

    java.util.List<Invoice> findByContract_ContractId(Integer contractId);
}
