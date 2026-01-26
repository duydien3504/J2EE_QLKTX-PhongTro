package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetResponse {
    private Integer assetId;
    private String assetName;
    private String assetCode;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
}
