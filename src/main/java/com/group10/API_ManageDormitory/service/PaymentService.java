package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.entity.Payment;
import com.group10.API_ManageDormitory.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public List<Payment> getAllPayments(){
        return paymentRepository.findAll();
    }

    public Payment createPayment(Payment payment){
        return paymentRepository.save(payment);
    }

    public void deletePayment(Integer id){
        paymentRepository.deleteById(id);
    }
}
