package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.entity.*;
import com.group10.API_ManageDormitory.dtos.response.InvoiceResponse;
import com.group10.API_ManageDormitory.mapper.InvoiceMapper;
import com.group10.API_ManageDormitory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.group10.API_ManageDormitory.dtos.response.PageResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final InvoiceMapper invoiceMapper;
    private final NotificationService notificationService;

    public PageResponse<InvoiceResponse> getAllInvoices(int page, int size) {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN") || 
                         com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("ADMIN");
        boolean isManageRole = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_STAFF") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("STAFF");

        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> invoicePage = invoiceRepository.findAllCustom(username, isAdmin, isManageRole, pageable);

        List<InvoiceResponse> data = invoicePage.getContent().stream()
                .map(invoiceMapper::toInvoiceResponse)
                .collect(Collectors.toList());

        return PageResponse.<InvoiceResponse>builder()
                .data(data)
                .totalElements(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .currentPage(invoicePage.getNumber())
                .pageSize(invoicePage.getSize())
                .build();
    }

    public InvoiceResponse getInvoiceById(Integer id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        checkInvoiceOwnership(invoice);
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

        checkContractOwnership(contract);

        // gán contract chuẩn từ database
        invoice.setContract(contract);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Notify Tenant about new invoice
        if (contract.getRoom() != null) {
            List<ContractTenant> contractTenants = contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(contract.getContractId());
            contractTenants.forEach(ct -> {
                if (ct.getTenant() != null && ct.getTenant().getUser() != null) {
                    notificationService.createNotification(com.group10.API_ManageDormitory.dtos.request.NotificationRequest.builder()
                            .title("Thông báo hóa đơn mới - Phòng " + contract.getRoom().getRoomNumber())
                            .content("Bạn có một hóa đơn mới cho tháng " + invoice.getMonth() + "/" + invoice.getYear() + 
                                    ". Tổng số tiền: " + new java.text.DecimalFormat("#,###").format(invoice.getTotalAmount()) + " VNĐ. Vui lòng kiểm tra và thanh toán.")
                            .type("INVOICE")
                            .userIds(java.util.List.of(ct.getTenant().getUser().getUserId()))
                            .build());
                }
            });
        }

        return invoiceMapper.toInvoiceResponse(savedInvoice);
    }

    public InvoiceResponse updateInvoice(Integer id, Invoice invoice) {

        Invoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        checkInvoiceOwnership(existing);

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
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        checkInvoiceOwnership(invoice);
        invoiceRepository.delete(invoice);
    }

    private void checkInvoiceOwnership(Invoice invoice) {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN") || 
                         com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("ADMIN");
        boolean isManageRole = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_STAFF") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("STAFF");

        if (isAdmin || !isManageRole || username == null) return;

        if (!isInvoiceManagedBy(invoice, username)) {
            throw new com.group10.API_ManageDormitory.exception.AppException(com.group10.API_ManageDormitory.exception.ErrorCode.ACCESS_DENIED_TO_RESOURCE);
        }
    }

    private void checkContractOwnership(Contract contract) {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN") || 
                         com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("ADMIN");
        boolean isManageRole = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_STAFF") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("STAFF");

        if (isAdmin || !isManageRole || username == null) return;

        if (!isContractManagedBy(contract, username)) {
            throw new com.group10.API_ManageDormitory.exception.AppException(com.group10.API_ManageDormitory.exception.ErrorCode.ACCESS_DENIED_TO_RESOURCE);
        }
    }

    private boolean isInvoiceManagedBy(Invoice invoice, String username) {
        return invoice.getContract() != null && isContractManagedBy(invoice.getContract(), username);
    }

    private boolean isContractManagedBy(Contract contract, String username) {
        return contract.getRoom() != null && 
               contract.getRoom().getFloor() != null && 
               contract.getRoom().getFloor().getBuilding() != null && 
               contract.getRoom().getFloor().getBuilding().getManager() != null && 
               contract.getRoom().getFloor().getBuilding().getManager().getUsername().equals(username);
    }
}