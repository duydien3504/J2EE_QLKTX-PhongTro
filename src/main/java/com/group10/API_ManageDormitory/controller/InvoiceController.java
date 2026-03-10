package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.entity.Invoice;
import com.group10.API_ManageDormitory.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;


    @GetMapping
    public List<Invoice> getAllInvoices(){
        return invoiceService.getAllInvoices();
    }


    @GetMapping("/{id}")
    public Invoice getInvoiceById(@PathVariable Integer id){
        return invoiceService.getInvoiceById(id);
    }


    @PostMapping
    public Invoice createInvoice(@RequestBody Invoice invoice){
        return invoiceService.createInvoice(invoice);
    }


    @PutMapping("/{id}")
    public Invoice updateInvoice(@PathVariable Integer id,
                                 @RequestBody Invoice invoice){
        return invoiceService.updateInvoice(id, invoice);
    }


    @DeleteMapping("/{id}")
    public void deleteInvoice(@PathVariable Integer id){
        invoiceService.deleteInvoice(id);
    }
}