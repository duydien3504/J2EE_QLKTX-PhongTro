package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @Size(min = 3, message = "USERNAME_INVALID")
    private String username;

    @Size(min = 8, message = "INVALID_PASSWORD")
    private String password;

    private String fullName;
    private String email;
    private String phoneNumber;
}
