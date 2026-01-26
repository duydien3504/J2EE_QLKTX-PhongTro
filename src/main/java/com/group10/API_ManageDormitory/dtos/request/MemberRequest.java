package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberRequest {
    @NotNull(message = "TENANT_ID_REQUIRED")
    private Integer tenantId;
}
