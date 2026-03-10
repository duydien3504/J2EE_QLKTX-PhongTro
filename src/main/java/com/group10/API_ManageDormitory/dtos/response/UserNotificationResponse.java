package com.group10.API_ManageDormitory.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotificationResponse {
    private Integer id;
    private Integer notificationId;
    private String title;
    private String content;
    private String type;
    private LocalDateTime createdDate;
    private Boolean isRead;
}
