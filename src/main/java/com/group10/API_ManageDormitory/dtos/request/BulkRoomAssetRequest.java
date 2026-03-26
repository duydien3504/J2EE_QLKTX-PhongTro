package com.group10.API_ManageDormitory.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkRoomAssetRequest {
    private Integer assetId;
    private Integer buildingId;
    private Integer roomTypeId; // Optional
    private Integer quantity;
    private String conditionStatus;
}
