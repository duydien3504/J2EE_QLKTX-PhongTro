package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {

        @NotNull(message = "Room ID không được để trống")
        private Long roomId;

        @Positive(message = "Electricity fee phải > 0")
        private Double electricityFee ;

        @Positive(message = "Water fee phải > 0")
        private Double waterFee;

        @Positive(message = "Service fee phải > 0")
        private Double serviceFee;


}

