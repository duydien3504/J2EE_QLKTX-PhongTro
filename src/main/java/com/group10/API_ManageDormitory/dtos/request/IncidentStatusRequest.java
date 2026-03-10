package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentStatusRequest {
    @NotBlank(message = "Status cannot be blank")
    private String status;
}
