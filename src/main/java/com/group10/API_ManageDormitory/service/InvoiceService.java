package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.entity.Contract;
import com.group10.API_ManageDormitory.entity.Invoice;
import com.group10.API_ManageDormitory.repository.ContractRepository;
import com.group10.API_ManageDormitory.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;


    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }


    public Invoice getInvoiceById(Integer id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }


    public Invoice createInvoice(Invoice invoice) {

        Integer contractId = invoice.getContract().getContractId();

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        invoice.setContract(contract);

        return invoiceRepository.save(invoice);
    }


    public Invoice updateInvoice(Integer id, Invoice invoice) {

        Invoice existing = getInvoiceById(id);

        existing.setMonth(invoice.getMonth());
        existing.setYear(invoice.getYear());
        existing.setCreatedDate(invoice.getCreatedDate());
        existing.setDueDate(invoice.getDueDate());
        existing.setTotalAmount(invoice.getTotalAmount());
        existing.setPaymentStatus(invoice.getPaymentStatus());
        existing.setNotes(invoice.getNotes());

        return invoiceRepository.save(existing);
    }


    public void deleteInvoice(Integer id) {
        invoiceRepository.deleteById(id);
    }
}