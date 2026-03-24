package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRegistrationRequest {
    @NotNull(message = "Room ID is required")
    private Integer roomId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // MOMO or CASH
}
