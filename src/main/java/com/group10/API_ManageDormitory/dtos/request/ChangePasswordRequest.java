package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {
    @NotBlank(message = "PASSWORD_INVALID")
    @Size(min = 6, message = "PASSWORD_INVALID")
    private String currentPassword;

    @NotBlank(message = "PASSWORD_INVALID")
    @Size(min = 6, message = "PASSWORD_INVALID")
    private String newPassword;
}
