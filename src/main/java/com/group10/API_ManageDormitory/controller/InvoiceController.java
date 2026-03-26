package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.InvoiceResponse;
import com.group10.API_ManageDormitory.dtos.response.PageResponse;
import com.group10.API_ManageDormitory.entity.Invoice;
import com.group10.API_ManageDormitory.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<PageResponse<InvoiceResponse>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) String roomNumber
    ){
        return ApiResponse.<PageResponse<InvoiceResponse>>builder()
                .result(invoiceService.getAllInvoices(page, size, month, year, status, buildingId, roomNumber))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF') or hasAuthority('SCOPE_TENANT')")
    public ApiResponse<InvoiceResponse> getInvoiceById(@PathVariable Integer id){
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.getInvoiceById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<InvoiceResponse> createInvoice(@Valid @RequestBody Invoice invoice){
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.createInvoice(invoice))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<InvoiceResponse> updateInvoice(@PathVariable Integer id,
                                 @Valid @RequestBody Invoice invoice){
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.updateInvoice(id, invoice))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public void deleteInvoice(@PathVariable Integer id){
        invoiceService.deleteInvoice(id);
    }
}