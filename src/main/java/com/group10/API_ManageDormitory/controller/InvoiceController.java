package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.InvoiceResponse;
import com.group10.API_ManageDormitory.entity.Invoice;
import com.group10.API_ManageDormitory.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<List<InvoiceResponse>> getAllInvoices(){
        return ApiResponse.<List<InvoiceResponse>>builder()
                .result(invoiceService.getAllInvoices())
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
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<InvoiceResponse> createInvoice(@Valid @RequestBody Invoice invoice){
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.createInvoice(invoice))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public ApiResponse<InvoiceResponse> updateInvoice(@PathVariable Integer id,
                                 @Valid @RequestBody Invoice invoice){
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.updateInvoice(id, invoice))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public void deleteInvoice(@PathVariable Integer id){
        invoiceService.deleteInvoice(id);
    }
}