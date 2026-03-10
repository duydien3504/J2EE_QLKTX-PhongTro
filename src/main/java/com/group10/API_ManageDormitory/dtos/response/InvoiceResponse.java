package com.group10.API_ManageDormitory.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private Long id ;
    private String roomNumber;
    private Double electricityFee;
    private Double waterFee;
    private Double serviceFee ;
    private Double totalAmount ;
    private String status ;

}


