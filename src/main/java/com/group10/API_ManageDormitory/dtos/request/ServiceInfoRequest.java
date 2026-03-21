package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceInfoRequest {
    @NotBlank(message = "SERVICE_NAME_REQUIRED")
    private String serviceName;

    @NotBlank(message = "UNIT_REQUIRED")
    private String unit;

    private String calculationMethod;

    private String icon;
}
