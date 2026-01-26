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
public class RoomTypeResponse {
    private Integer roomTypeId;
    private String typeName;
    private BigDecimal basePrice;
    private Double area;
    private Integer maxOccupancy;
    private String description;
}
