package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantRequest {
    @NotBlank(message = "FULL_NAME_REQUIRED")
    private String fullName;

    private String phoneNumber;

    @Email(message = "EMAIL_INVALID")
    private String email;

    private String hometown;

    @NotBlank(message = "CCCD_REQUIRED")
    private String identityCardNumber;

    // Optional User ID if linking to a system user
    private Integer userId;
}
