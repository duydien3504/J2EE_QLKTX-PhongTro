package com.group10.API_ManageDormitory.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String avatar; // Not in entity yet but requested, I'll ignore or add if user wants. But Entity
                           // checks show no avatar column. I will just handle fullName/phone first.
}
