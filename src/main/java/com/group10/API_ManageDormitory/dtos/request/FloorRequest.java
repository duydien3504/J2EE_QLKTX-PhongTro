package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorRequest {
    @NotBlank(message = "FLOOR_NAME_REQUIRED")
    private String floorName;

    @NotNull(message = "BUILDING_ID_REQUIRED")
    private Integer buildingId;
}
