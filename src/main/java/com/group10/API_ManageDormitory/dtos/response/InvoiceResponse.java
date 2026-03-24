package com.group10.API_ManageDormitory.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private Integer invoiceId;
    private ContractSummary contract;
    private Integer month;
    private Integer year;
    private java.time.LocalDate createdDate;
    private java.time.LocalDate dueDate;
    private java.math.BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private String lastTransactionStatus;
    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContractSummary {
        private Integer contractId;
        private RoomSummary room;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomSummary {
        private Integer roomId;
        private String roomNumber;
    }
}
