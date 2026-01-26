package com.group10.API_ManageDormitory.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "PASSWORD_INVALID")
    String password;

    String fullName;
    String email;
    String phoneNumber;
    String roleName; // Optional, defaults to STAFF or similar? Task says "Create Staff".
}
