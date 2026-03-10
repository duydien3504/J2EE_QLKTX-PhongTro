package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.entity.Payment;
import com.group10.API_ManageDormitory.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @GetMapping
    public List<Payment> getAllPayments(){
        return paymentService.getAllPayments();
    }


    @PostMapping
    public Payment createPayment(@RequestBody Payment payment){
        return paymentService.createPayment(payment);
    }


    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable Integer id){
        paymentService.deletePayment(id);
    }

}