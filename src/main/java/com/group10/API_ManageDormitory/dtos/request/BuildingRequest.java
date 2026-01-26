package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingRequest {
    @NotBlank(message = "BUILDING_NAME_REQUIRED")
    private String buildingName;

    private String address;

    private Integer managerId; // Create/Update assumes user exists in DB.

    @Min(value = 0, message = "TOTAL_FLOORS_INVALID")
    private Integer totalFloors;
}
