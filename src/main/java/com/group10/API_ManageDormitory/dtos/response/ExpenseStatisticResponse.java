package com.group10.API_ManageDormitory.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseStatisticResponse {
    private String expenseType;
    private BigDecimal totalAmount;
}
