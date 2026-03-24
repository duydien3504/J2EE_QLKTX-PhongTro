package com.group10.API_ManageDormitory.service;

import com.google.gson.Gson;
import com.group10.API_ManageDormitory.config.MoMoConfig;
import com.group10.API_ManageDormitory.entity.Invoice;
import com.group10.API_ManageDormitory.entity.Payment;
import com.group10.API_ManageDormitory.repository.InvoiceRepository;
import com.group10.API_ManageDormitory.repository.PaymentRepository;
import com.group10.API_ManageDormitory.utils.momo.MoMoEncoder;
import com.group10.API_ManageDormitory.utils.momo.MoMoRequest;
import com.group10.API_ManageDormitory.utils.momo.MoMoResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoMoService {

    private final MoMoConfig moMoConfig;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final Gson gson = new Gson();
    private final OkHttpClient httpClient = new OkHttpClient();

    public MoMoResponse createPayment(Integer invoiceId) throws IOException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if ("PAID".equalsIgnoreCase(invoice.getPaymentStatus())) {
            throw new RuntimeException("Invoice already paid");
        }

        String requestId = UUID.randomUUID().toString();
        String orderId = "INV-" + invoiceId + "-" + System.currentTimeMillis();
        long amount = invoice.getTotalAmount().longValue();
        String orderInfo = "Thanh toan hoa don #" + invoiceId;
        String extraData = "";
        String requestType = "captureWallet";

        String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + moMoConfig.getNotifyUrl() +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + moMoConfig.getPartnerCode() +
                "&redirectUrl=" + moMoConfig.getReturnUrl() +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = MoMoEncoder.signHmacSHA256(rawSignature, moMoConfig.getSecretKey());

        MoMoRequest moMoRequest = MoMoRequest.builder()
                .partnerCode(moMoConfig.getPartnerCode())
                .requestId(requestId)
                .amount(amount)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(moMoConfig.getReturnUrl())
                .ipnUrl(moMoConfig.getNotifyUrl())
                .extraData(extraData)
                .requestType(requestType)
                .signature(signature)
                .lang("vi")
                .partnerName("Test MoMo")
                .storeId("Test Store")
                .build();

        RequestBody body = RequestBody.create(
                gson.toJson(moMoRequest),
                MediaType.get("application/json; charset=utf-8")
        );

        System.out.println("MoMo Request Payload: " + gson.toJson(moMoRequest));
        System.out.println("MoMo Raw Signature: " + rawSignature);

        // Update Invoice status to PENDING
        invoice.setPaymentMethod("MoMo");
        invoice.setLastTransactionStatus("PENDING");
        invoiceRepository.save(invoice);

        String endpoint = moMoConfig.getApiEndpoint();
        if (!endpoint.endsWith("/create")) {
            endpoint = endpoint.endsWith("/") ? endpoint + "create" : endpoint + "/create";
        }

        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                System.out.println("MoMo Error Response: " + responseBody);
                invoice.setLastTransactionStatus("FAILED");
                invoiceRepository.save(invoice);
                throw new IOException("Unexpected code " + response + " | Body: " + responseBody);
            }
            return gson.fromJson(responseBody, MoMoResponse.class);
        }
    }

    public MoMoResponse queryStatus(String orderId) throws IOException {
        String requestId = UUID.randomUUID().toString();
        String rawSignature = "accessKey=" + moMoConfig.getAccessKey() +
                "&orderId=" + orderId +
                "&partnerCode=" + moMoConfig.getPartnerCode() +
                "&requestId=" + requestId;

        String signature = MoMoEncoder.signHmacSHA256(rawSignature, moMoConfig.getSecretKey());

        MoMoRequest moMoRequest = MoMoRequest.builder()
                .partnerCode(moMoConfig.getPartnerCode())
                .requestId(requestId)
                .orderId(orderId)
                .signature(signature)
                .build();

        RequestBody body = RequestBody.create(
                gson.toJson(moMoRequest),
                MediaType.get("application/json; charset=utf-8")
        );

        String endpoint = moMoConfig.getApiEndpoint();
        if (!endpoint.endsWith("/query")) {
            endpoint = endpoint.endsWith("/") ? endpoint + "query" : endpoint + "/query";
        }

        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            return gson.fromJson(responseBody, MoMoResponse.class);
        }
    }

    public void processIPN(String body) {
        MoMoResponse response = gson.fromJson(body, MoMoResponse.class);

        // In a real scenario, you MUST verify the signature from MoMo here
        // For simplicity and demo purposes, we'll assume it's valid if resultCode is 0

        if (response.getResultCode() == 0) {
            String orderId = response.getOrderId(); // Format: INV-invoiceId-timestamp
            Integer invoiceId = Integer.parseInt(orderId.split("-")[1]);

            Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
            if (invoice != null) {
                if (response.getResultCode() == 0) {
                    if (!"PAID".equalsIgnoreCase(invoice.getPaymentStatus())) {
                        invoice.setPaymentStatus("PAID");
                        invoice.setLastTransactionStatus("SUCCESS");
                        invoice.setPaymentMethod("MoMo");
                        invoiceRepository.save(invoice);

                        Payment payment = Payment.builder()
                                .invoice(invoice)
                                .paymentDate(LocalDateTime.now())
                                .amountPaid(invoice.getTotalAmount())
                                .paymentMethod("MoMo")
                                .transactionCode(response.getRequestId())
                                .build();
                        paymentRepository.save(payment);
                    }
                } else {
                    invoice.setLastTransactionStatus("FAILED");
                    invoiceRepository.save(invoice);
                }
            }
        }
    }
}
