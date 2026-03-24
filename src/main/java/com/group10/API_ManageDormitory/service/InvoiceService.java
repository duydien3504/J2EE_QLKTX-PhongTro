package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.entity.Contract;
import com.group10.API_ManageDormitory.entity.Invoice;
import com.group10.API_ManageDormitory.dtos.response.InvoiceResponse;
import com.group10.API_ManageDormitory.mapper.InvoiceMapper;
import com.group10.API_ManageDormitory.repository.ContractRepository;
import com.group10.API_ManageDormitory.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final InvoiceMapper invoiceMapper;

    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(invoiceMapper::toInvoiceResponse)
                .collect(Collectors.toList());
    }

    public InvoiceResponse getInvoiceById(Integer id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    public InvoiceResponse createInvoice(Invoice invoice) {

        // kiểm tra contract có tồn tại trong request không
        if (invoice.getContract() == null || invoice.getContract().getContractId() == null) {
            throw new RuntimeException("Contract ID is required");
        }

        // tìm contract trong database
        Contract contract = contractRepository.findById(invoice.getContract().getContractId())
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        // gán contract chuẩn từ database
        invoice.setContract(contract);

        return invoiceMapper.toInvoiceResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse updateInvoice(Integer id, Invoice invoice) {

        Invoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        existing.setMonth(invoice.getMonth());
        existing.setYear(invoice.getYear());
        existing.setCreatedDate(invoice.getCreatedDate());
        existing.setDueDate(invoice.getDueDate());
        existing.setTotalAmount(invoice.getTotalAmount());
        existing.setPaymentStatus(invoice.getPaymentStatus());
        existing.setNotes(invoice.getNotes());

        Invoice saved = invoiceRepository.save(existing);
        
        // Cập nhật trạng thái hợp đồng nếu là hóa đơn cọc và đã thanh toán
        if ("PAID".equalsIgnoreCase(saved.getPaymentStatus())) {
            Contract contract = saved.getContract();
            if (contract != null && "WAITING_DEPOSIT".equalsIgnoreCase(contract.getContractStatus())) {
                contract.setContractStatus("ACTIVE");
                contractRepository.save(contract);
            }
        }

        return invoiceMapper.toInvoiceResponse(saved);
    }

    public void deleteInvoice(Integer id) {
        invoiceRepository.deleteById(id);
    }
}