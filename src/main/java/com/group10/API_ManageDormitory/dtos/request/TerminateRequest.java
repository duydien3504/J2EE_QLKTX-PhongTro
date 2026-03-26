package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminateRequest {
    private BigDecimal deductionAmount;
    private String deductionReason;
    private BigDecimal finalElectricityReading;
    private BigDecimal finalWaterReading;
}
