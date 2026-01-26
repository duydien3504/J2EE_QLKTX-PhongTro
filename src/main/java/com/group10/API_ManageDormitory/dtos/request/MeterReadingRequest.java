package com.group10.API_ManageDormitory.dtos.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReadingRequest {
    @NotNull(message = "ROOM_ID_REQUIRED")
    private Integer roomId;

    @NotNull(message = "SERVICE_ID_REQUIRED")
    private Integer serviceId;

    @NotNull(message = "CURRENT_INDEX_REQUIRED")
    private Double currentIndex;

    private Double previousIndex; // Optional, can be fetched automatically

    private LocalDate readingDate;

    private String imageProof;
}
