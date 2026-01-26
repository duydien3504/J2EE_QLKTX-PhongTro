package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingResponse {
    private Integer buildingId;
    private String buildingName;
    private String address;
    private Integer managerId;
    private String managerName;
    private Integer totalFloors;
}
