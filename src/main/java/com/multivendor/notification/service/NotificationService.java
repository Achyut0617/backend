package com.multivendor.notification.service;

import com.multivendor.common.enums.UserType;
import com.multivendor.notification.entity.Notification;
import com.multivendor.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Async
    public void sendNotification(UserType userType, Long userId, String title, String message, String notificationType) {
        Notification notification = Notification.builder()
                .userType(userType)
                .userId(userId)
                .title(title)
                .message(message)
                .notificationType(notificationType)
                .build();
        notificationRepository.save(notification);
    }

    public Page<Notification> getNotifications(UserType userType, Long userId, Pageable pageable) {
        return notificationRepository.findByUserTypeAndUserIdOrderByCreatedAtDesc(userType, userId, pageable);
    }

    public long getUnreadCount(UserType userType, Long userId) {
        return notificationRepository.countByUserTypeAndUserIdAndIsReadFalse(userType, userId);
    }

    @Transactional
    public void markAllAsRead(UserType userType, Long userId) {
        notificationRepository.markAllAsRead(userType, userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId, UserType userType) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId) && n.getUserType() == userType) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }
}
