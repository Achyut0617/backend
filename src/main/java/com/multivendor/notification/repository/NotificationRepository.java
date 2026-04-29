package com.multivendor.notification.repository;

import com.multivendor.common.enums.UserType;
import com.multivendor.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserTypeAndUserIdOrderByCreatedAtDesc(UserType type, Long userId, Pageable pageable);
    long countByUserTypeAndUserIdAndIsReadFalse(UserType type, Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userType = :type AND n.userId = :userId")
    void markAllAsRead(UserType type, Long userId);
}
