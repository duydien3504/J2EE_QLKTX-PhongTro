package com.group10.API_ManageDormitory.mapper;

import com.group10.API_ManageDormitory.dtos.response.InvoiceResponse;
import com.group10.API_ManageDormitory.entity.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {
    public InvoiceResponse toInvoiceResponse(Invoice invoice) {
        if (invoice == null) return null;

        InvoiceResponse.ContractSummary contractSummary = null;
        if (invoice.getContract() != null) {
            InvoiceResponse.RoomSummary roomSummary = null;
            if (invoice.getContract().getRoom() != null) {
                roomSummary = InvoiceResponse.RoomSummary.builder()
                        .roomId(invoice.getContract().getRoom().getRoomId())
                        .roomNumber(invoice.getContract().getRoom().getRoomNumber())
                        .build();
            }

            contractSummary = InvoiceResponse.ContractSummary.builder()
                    .contractId(invoice.getContract().getContractId())
                    .room(roomSummary)
                    .build();
        }

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .contract(contractSummary)
                .month(invoice.getMonth())
                .year(invoice.getYear())
                .createdDate(invoice.getCreatedDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .paymentStatus(invoice.getPaymentStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .lastTransactionStatus(invoice.getLastTransactionStatus())
                .notes(invoice.getNotes())
                .build();
    }
}
