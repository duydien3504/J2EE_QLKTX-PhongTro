package com.group10.API_ManageDormitory.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {
    private Integer incidentId;
    private Integer roomId;
    private String roomNumber;
    private Integer tenantId;
    private String tenantName;
    private String description;
    private String status;
    private LocalDateTime reportedDate;
}
