package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorResponse {
    private Integer floorId;
    private String floorName;
    private Integer buildingId;
    private String buildingName;
}
