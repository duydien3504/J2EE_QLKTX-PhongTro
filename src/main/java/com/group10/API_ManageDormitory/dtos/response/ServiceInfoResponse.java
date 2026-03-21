package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceInfoResponse {
    private Integer serviceId;
    private String serviceName;
    private String unit;
    private String calculationMethod;
    private String icon;
}
