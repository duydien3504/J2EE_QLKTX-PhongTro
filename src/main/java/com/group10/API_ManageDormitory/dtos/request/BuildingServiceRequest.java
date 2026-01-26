package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingServiceRequest {
    @NotNull(message = "SERVICE_ID_REQUIRED")
    private Integer serviceId;

    @NotNull(message = "PRICE_REQUIRED")
    private BigDecimal unitPrice;
}
