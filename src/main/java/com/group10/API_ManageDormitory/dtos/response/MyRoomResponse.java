package com.group10.API_ManageDormitory.dtos.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyRoomResponse {
    private RoomResponse room;
    private List<TenantResponse> roommates;
    private List<BuildingServiceResponse> services;
    private List<InvoiceResponse> recentInvoices;
    private ContractResponse contract;
}
