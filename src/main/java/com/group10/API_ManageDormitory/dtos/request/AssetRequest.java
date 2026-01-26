package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetRequest {
    @NotBlank(message = "ASSET_NAME_REQUIRED")
    private String assetName;

    private String assetCode;
    private BigDecimal purchasePrice;
    private LocalDate purchaseDate;
}
