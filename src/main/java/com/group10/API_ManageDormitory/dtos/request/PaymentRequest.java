package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "Invoice ID không được để trống")
    private Long invoiceId ;

    @NotNull(message = "số tiền không được để trống")
    @Positive(message = "số tiền phải lớn hơn 0")
    private Double amount ;

    @NotBlank(message = "phương thức thanh toán không được để trống" )
    private String paymentMethod;
}
