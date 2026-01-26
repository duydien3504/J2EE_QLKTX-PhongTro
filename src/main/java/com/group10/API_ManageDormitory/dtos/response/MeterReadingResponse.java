package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReadingResponse {
    private Integer meterReadingId;
    private Integer roomId;
    private String roomNumber;
    private Integer serviceId;
    private String serviceName;
    private Double previousIndex;
    private Double currentIndex;
    private Double usage;
    private LocalDate readingDate;
    private String imageProof;
}
