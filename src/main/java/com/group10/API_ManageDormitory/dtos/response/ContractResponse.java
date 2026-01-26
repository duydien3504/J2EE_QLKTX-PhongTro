package com.group10.API_ManageDormitory.dtos.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponse {
    private Integer contractId;
    private Integer roomId;
    private String roomNumber;
    private BigDecimal rentalPrice;
    private BigDecimal depositAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractStatus;
    private List<ContractMemberResponse> members;
}
