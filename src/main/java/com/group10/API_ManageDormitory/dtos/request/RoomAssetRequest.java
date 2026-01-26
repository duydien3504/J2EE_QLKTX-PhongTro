package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAssetRequest {
    @NotNull(message = "ROOM_ID_REQUIRED")
    private Integer roomId;

    @NotNull(message = "ASSET_ID_REQUIRED")
    private Integer assetId;

    @NotNull(message = "QUANTITY_REQUIRED")
    private Integer quantity;

    private String conditionStatus; // e.g. "GOOD", "BROKEN"
}
