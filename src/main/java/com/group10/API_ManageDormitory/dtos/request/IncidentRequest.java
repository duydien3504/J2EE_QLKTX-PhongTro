package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentRequest {
    @NotNull(message = "Room is required")
    private Integer roomId;

    @NotNull(message = "Tenant is required")
    private Integer tenantId;

    @NotBlank(message = "Description cannot be blank")
    private String description;
}
