package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    private Integer roomId;
    private String roomNumber;
    private Integer floorId;
    private String floorName;
    private Integer buildingId;
    private String buildingName;
    private Integer roomTypeId;
    private String roomTypeName;
    private BigDecimal price;
    private String currentStatus;
    private String description;
    private List<String> imageUrls;
}

