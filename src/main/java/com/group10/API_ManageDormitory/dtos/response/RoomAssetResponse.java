package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAssetResponse {
    private Integer roomAssetId;
    private Integer roomId;
    private String roomNumber;
    private Integer assetId;
    private String assetName;
    private Integer quantity;
    private String conditionStatus;
}
