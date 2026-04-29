package com.multivendor.notification.controller;

import com.multivendor.common.enums.UserType;
import com.multivendor.common.response.ApiResponse;
import com.multivendor.config.security.JwtAuthenticationToken;
import com.multivendor.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/notifications")
@RequiredArgsConstructor
public class VendorNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<?>> getNotifications(
            JwtAuthenticationToken auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getNotifications(UserType.VENDOR, auth.getUserId(), PageRequest.of(page, size)),
                "Notifications retrieved"));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Long>> unreadCount(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadCount(UserType.VENDOR, auth.getUserId()), "OK"));
    }

    @PostMapping("/mark-all-read")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> markAllRead(JwtAuthenticationToken auth) {
        notificationService.markAllAsRead(UserType.VENDOR, auth.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Marked as read"));
    }
}
