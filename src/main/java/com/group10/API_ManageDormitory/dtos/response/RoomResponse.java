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
public class RoomResponse {
    private Integer roomId;
    private String roomNumber;
    private Integer floorId;
    private String floorName;
    private String buildingName;
    private Integer roomTypeId;
    private String roomTypeName;
    private BigDecimal price;
    private String currentStatus;
}
