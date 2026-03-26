package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidationResponse {
    private Integer contractId;
    private LocalDate liquidationDate;
    private BigDecimal depositAmount;
    private BigDecimal deductionAmount;
    private BigDecimal refundAmount;
    private String deductionReason;
    private String contractStatus;
    private BigDecimal finalElectricityReading;
    private BigDecimal finalWaterReading;
}
