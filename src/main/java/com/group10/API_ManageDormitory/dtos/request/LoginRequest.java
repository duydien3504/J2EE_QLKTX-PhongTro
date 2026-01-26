package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @NotNull(message = "USERNAME_INVALID")
    private String username;

    @NotNull(message = "INVALID_PASSWORD")
    private String password;
}
