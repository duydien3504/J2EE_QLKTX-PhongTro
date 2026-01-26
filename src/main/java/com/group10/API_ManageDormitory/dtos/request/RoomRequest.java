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
public class RoomRequest {
    @NotBlank(message = "ROOM_NUMBER_REQUIRED")
    private String roomNumber;

    @NotNull(message = "FLOOR_ID_REQUIRED")
    private Integer floorId;

    @NotNull(message = "ROOM_TYPE_ID_REQUIRED")
    private Integer roomTypeId;

    private String currentStatus; // AVAILABLE, OCCUPIED, MAINTENANCE
}
