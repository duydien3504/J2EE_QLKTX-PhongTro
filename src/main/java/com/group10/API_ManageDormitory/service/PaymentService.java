package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.entity.Payment;
import com.group10.API_ManageDormitory.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.group10.API_ManageDormitory.dtos.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PageResponse<Payment> getAllPayments(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);
        
        return PageResponse.<Payment>builder()
                .data(paymentPage.getContent())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .currentPage(paymentPage.getNumber())
                .pageSize(paymentPage.getSize())
                .build();
    }

    public Payment createPayment(Payment payment){
        return paymentRepository.save(payment);
    }

    public void deletePayment(Integer id){
        paymentRepository.deleteById(id);
    }
}
