package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractMemberResponse {
    private Integer contractTenantId;
    private Integer tenantId;
    private String fullName;
    private Boolean isRepresentative;
    private String phoneNumber;
}
