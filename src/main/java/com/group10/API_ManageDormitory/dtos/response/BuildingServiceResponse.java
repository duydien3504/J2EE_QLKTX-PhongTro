package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingServiceResponse {
    private Integer buildingServiceId;
    private Integer buildingId;
    private Integer serviceId;
    private String serviceName;
    private String unit;
    private BigDecimal unitPrice;
}
