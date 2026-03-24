package com.group10.API_ManageDormitory.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyByBuildingResponse {
    private String buildingName;
    private long totalRooms;
    private long occupiedRooms;
    private long vacantRooms;
}
