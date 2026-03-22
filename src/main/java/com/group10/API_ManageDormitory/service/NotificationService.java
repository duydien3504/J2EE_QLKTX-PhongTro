package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.NotificationRequest;
import com.group10.API_ManageDormitory.dtos.response.NotificationResponse;
import com.group10.API_ManageDormitory.dtos.response.UserNotificationResponse;
import com.group10.API_ManageDormitory.entity.Notification;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.entity.UserNotification;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.NotificationRepository;
import com.group10.API_ManageDormitory.repository.UserNotificationRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        List<User> targetUsers;
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            // Send to all users if specific users not provided
            targetUsers = userRepository.findAll();
        } else {
            targetUsers = userRepository.findAllById(request.getUserIds());
        }

        List<UserNotification> userNotifications = targetUsers.stream()
                .map(user -> UserNotification.builder()
                        .user(user)
                        .notification(savedNotification)
                        .build())
                .collect(Collectors.toList());

        userNotificationRepository.saveAll(userNotifications);

        return toNotificationResponse(savedNotification);
    }

    public List<UserNotificationResponse> getUserNotifications(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        return userNotificationRepository.findByUser_UserIdOrderByNotification_CreatedDateDesc(userId).stream()
                .map(this::toUserNotificationResponse)
                .collect(Collectors.toList());
    }

    public void markAsRead(Integer userNotificationId) {
        UserNotification un = userNotificationRepository.findById(userNotificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        un.setIsRead(true);
        userNotificationRepository.save(un);
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .createdDate(notification.getCreatedDate())
                .build();
    }

    private UserNotificationResponse toUserNotificationResponse(UserNotification un) {
        return UserNotificationResponse.builder()
                .id(un.getId())
                .notificationId(un.getNotification().getNotificationId())
                .title(un.getNotification().getTitle())
                .content(un.getNotification().getContent())
                .type(un.getNotification().getType())
                .createdDate(un.getNotification().getCreatedDate())
                .isRead(un.getIsRead())
                .build();
    }
}
