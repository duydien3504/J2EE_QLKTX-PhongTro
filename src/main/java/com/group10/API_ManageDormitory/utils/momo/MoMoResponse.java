package com.group10.API_ManageDormitory.utils.momo;

import lombok.Data;

@Data
public class MoMoResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private long amount;
    private long responseTime;
    private String message;
    private int resultCode;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private String signature;
}
