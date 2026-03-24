package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Integer> {

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM InvoiceDetail d " +
           "WHERE d.invoice.month = :month AND d.invoice.year = :year " +
           "AND d.invoice.paymentStatus = 'PAID' " +
           "AND LOWER(d.serviceName) = 'tiền phòng'")
    BigDecimal getRentRevenueByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM InvoiceDetail d " +
           "WHERE d.invoice.month = :month AND d.invoice.year = :year " +
           "AND d.invoice.paymentStatus = 'PAID' " +
           "AND LOWER(d.serviceName) <> 'tiền phòng'")
    BigDecimal getServiceRevenueByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);
}

