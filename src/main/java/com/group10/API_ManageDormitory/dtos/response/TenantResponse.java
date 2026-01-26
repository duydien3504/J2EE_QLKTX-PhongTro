package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {
    private Integer tenantId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String hometown;
    private String identityCardNumber;
    private String identityCardImageFront;
    private String identityCardImageBack;
    private Integer userId;
    private String username;
}
