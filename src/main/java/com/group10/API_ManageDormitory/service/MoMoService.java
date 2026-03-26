package com.group10.API_ManageDormitory.service;

import com.google.gson.Gson;
import com.group10.API_ManageDormitory.config.MoMoConfig;
import com.group10.API_ManageDormitory.entity.*;
import com.group10.API_ManageDormitory.repository.*;
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
    private final ContractRepository contractRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final NotificationService notificationService;
    private final AccessValidationService accessValidationService;
    private Gson gson = new Gson();
    private OkHttpClient httpClient = new OkHttpClient();

    public MoMoResponse createPayment(Integer invoiceId) throws IOException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        accessValidationService.validateContractAccess(invoice.getContract());

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
        System.out.println("Querying MoMo status for OrderId: " + orderId);
        
        // Extract invoiceId from orderId (Format: INV-invoiceId-timestamp)
        try {
            String[] parts = orderId.split("-");
            if (parts.length >= 2) {
                Integer invoiceId = Integer.parseInt(parts[1]);
                Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
                if (invoice != null) {
                    accessValidationService.validateContractAccess(invoice.getContract());
                }
            }
        } catch (Exception e) {
            // If parsing fails, we still proceed to MoMo query but it will likely fail there too
            System.err.println("Failed to validate access for orderId: " + orderId);
        }

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
            System.out.println("MoMo Query Response: " + responseBody);
            MoMoResponse moMoResponse = gson.fromJson(responseBody, MoMoResponse.class);
            
            // Nếu query thấy thành công mà DB chưa cập nhật thì cập nhật luôn
            if (moMoResponse != null && moMoResponse.getResultCode() == 0) {
                processIPN(responseBody); // Tận dụng logic xử lý IPN để update DB
            }
            
            return moMoResponse;
        }
    }

    public void processIPN(String body) {
        System.out.println("MoMo IPN Received: " + body);
        try {
            MoMoResponse response = gson.fromJson(body, MoMoResponse.class);

            if (response == null) {
                System.out.println("MoMo IPN Error: Response body is null");
                return;
            }

            if (response.getResultCode() == 0) {
                String orderId = response.getOrderId(); // Format: INV-invoiceId-timestamp
                System.out.println("Processing successful payment for OrderId: " + orderId);
                
                String[] parts = orderId.split("-");
                if (parts.length < 2) {
                    System.out.println("MoMo IPN Error: Invalid OrderId format: " + orderId);
                    return;
                }
                
                Integer invoiceId = Integer.parseInt(parts[1]);
                Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
                
                if (invoice != null) {
                    System.out.println("Updating Invoice #" + invoiceId + " to PAID");
                    if (!"PAID".equalsIgnoreCase(invoice.getPaymentStatus())) {
                        invoice.setPaymentStatus("PAID");
                        invoice.setLastTransactionStatus("SUCCESS");
                        invoice.setPaymentMethod("MoMo");
                        invoiceRepository.save(invoice);

                        // Cập nhật trạng thái hợp đồng nếu là hóa đơn cọc
                        Contract contract = invoice.getContract();
                        if (contract != null && "WAITING_DEPOSIT".equalsIgnoreCase(contract.getContractStatus())) {
                            contract.setContractStatus("ACTIVE");
                            contractRepository.save(contract);
                            System.out.println("Contract Status updated to ACTIVE");
                        }

                        Payment payment = Payment.builder()
                                .invoice(invoice)
                                .paymentDate(LocalDateTime.now())
                                .amountPaid(invoice.getTotalAmount())
                                .paymentMethod("MoMo")
                                .transactionCode(response.getTransId() != null ? response.getTransId().toString() : response.getRequestId())
                                .build();
                        paymentRepository.save(payment);
                        System.out.println("Payment record created successfully");

                        // Notify Tenant and Manager
                        if (invoice.getContract() != null && invoice.getContract().getRoom() != null) {
                            String roomNum = invoice.getContract().getRoom().getRoomNumber();
                            String amountStr = new java.text.DecimalFormat("#,###").format(invoice.getTotalAmount());
                            
                            // 1. Notify Tenants
                            java.util.List<ContractTenant> members = 
                                contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(invoice.getContract().getContractId());
                            
                            members.forEach(m -> {
                                if (m.getTenant() != null && m.getTenant().getUser() != null) {
                                    notificationService.createNotification(com.group10.API_ManageDormitory.dtos.request.NotificationRequest.builder()
                                            .title("Thanh toán thành công - Phòng " + roomNum)
                                            .content("Hệ thống đã nhận được số tiền " + amountStr + " VNĐ cho hóa đơn tháng " + invoice.getMonth() + "/" + invoice.getYear() + ". Cảm ơn bạn!")
                                            .type("PAYMENT")
                                            .userIds(java.util.List.of(m.getTenant().getUser().getUserId()))
                                            .build());
                                }
                            });

                            // 2. Notify Building Manager
                            User manager = invoice.getContract().getRoom().getFloor().getBuilding().getManager();
                            if (manager != null) {
                                notificationService.createNotification(com.group10.API_ManageDormitory.dtos.request.NotificationRequest.builder()
                                        .title("Thông báo thanh toán - Phòng " + roomNum)
                                        .content("Phòng " + roomNum + " vừa thanh toán hóa đơn tháng " + invoice.getMonth() + "/" + invoice.getYear() + " (Số tiền: " + amountStr + " VNĐ).")
                                        .type("PAYMENT")
                                        .userIds(java.util.List.of(manager.getUserId()))
                                        .build());
                            }
                        }
                    } else {
                        System.out.println("Invoice #" + invoiceId + " was already marked as PAID");
                    }
                } else {
                    System.out.println("MoMo IPN Error: Invoice not found for ID: " + invoiceId);
                }
            } else {
                System.out.println("MoMo Payment Failed or Cancelled. ResultCode: " + response.getResultCode());
            }
        } catch (Exception e) {
            System.err.println("Error processing MoMo IPN: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
