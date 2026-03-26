package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.service.MoMoService;
import com.group10.API_ManageDormitory.utils.momo.MoMoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/payments/momo")
@RequiredArgsConstructor
public class PaymentController {

    private final MoMoService moMoService;

    @PostMapping("/create/{invoiceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MoMoResponse> createPayment(@PathVariable Integer invoiceId) throws IOException {
        MoMoResponse response = moMoService.createPayment(invoiceId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody String body) {
        moMoService.processIPN(body);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/query/{orderId}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF') or hasAuthority('SCOPE_TENANT')")
    public ResponseEntity<MoMoResponse> queryStatus(@PathVariable String orderId) throws IOException {
        MoMoResponse response = moMoService.queryStatus(orderId);
        return ResponseEntity.ok(response);
    }
}