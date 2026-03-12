package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {

        @NotNull(message = "Contract ID không được để trống")
        private Integer contractId;

        @NotNull(message = "Month không được để trống")
        private Integer month;

        @NotNull(message = "Year không được để trống")
        private Integer year;

        private LocalDate createdDate;

        private LocalDate dueDate;

        @Positive(message = "Total amount phải > 0")
        private BigDecimal totalAmount;

        private String paymentStatus;

        private String notes;
}