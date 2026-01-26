package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractRequest {
    @NotNull(message = "ROOM_ID_REQUIRED")
    private Integer roomId;

    @NotNull(message = "REPRESENTATIVE_TENANT_ID_REQUIRED")
    private Integer representativeTenantId;

    @NotNull(message = "START_DATE_REQUIRED")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "RENTAL_PRICE_REQUIRED")
    @Min(value = 0, message = "PRICE_INVALID")
    private BigDecimal rentalPrice;

    private BigDecimal depositAmount;
    private Integer paymentCycle; // Months
}
