package com.group10.API_ManageDormitory.dtos.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractRegistrationResponse {
    private ContractResponse contract;
    private InvoiceResponse depositInvoice;
    private String payUrl; // For MoMo
}
