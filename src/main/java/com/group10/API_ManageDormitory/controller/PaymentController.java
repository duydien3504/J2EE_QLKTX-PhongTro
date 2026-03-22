package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.entity.Payment;
import com.group10.API_ManageDormitory.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public List<Payment> getAllPayments(){
        return paymentService.getAllPayments();
    }


    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public Payment createPayment(@RequestBody Payment payment){
        return paymentService.createPayment(payment);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public void deletePayment(@PathVariable Integer id){
        paymentService.deletePayment(id);
    }

}