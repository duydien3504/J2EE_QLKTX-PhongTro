package com.group10.API_ManageDormitory.dtos.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String roleName;
    private Integer tenantId;
    private java.util.List<String> managedBuildings;
    // Hide password and strict role handling
}
