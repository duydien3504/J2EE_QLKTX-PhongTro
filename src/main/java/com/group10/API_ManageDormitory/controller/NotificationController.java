package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.NotificationRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.NotificationResponse;
import com.group10.API_ManageDormitory.dtos.response.UserNotificationResponse;
import com.group10.API_ManageDormitory.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ApiResponse<NotificationResponse> createNotification(@RequestBody @Valid NotificationRequest request) {
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService.createNotification(request))
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserNotificationResponse>> getUserNotifications(@PathVariable Integer userId) {
        return ApiResponse.<List<UserNotificationResponse>>builder()
                .result(notificationService.getUserNotifications(userId))
                .build();
    }

    @PutMapping("/{userNotificationId}/read")
    public ApiResponse<String> markAsRead(@PathVariable Integer userNotificationId) {
        notificationService.markAsRead(userNotificationId);
        return ApiResponse.<String>builder()
                .result("Notification marked as read successfully")
                .build();
    }
}
