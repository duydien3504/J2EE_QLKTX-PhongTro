package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeRequest {
    @NotBlank(message = "TYPE_NAME_REQUIRED")
    private String typeName;

    @NotNull(message = "PRICE_REQUIRED")
    @Min(value = 0, message = "PRICE_INVALID")
    private BigDecimal basePrice;

    private Double area;
    private Integer maxOccupancy;
    private String description;
}
